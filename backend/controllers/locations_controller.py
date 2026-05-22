from flask import Blueprint, request, jsonify
from services.location_service import LocationService
from database import Database
from helpers.format_helper import dict_format, dict_format_single
import json as _json
import traceback

locations_bp = Blueprint('locations', __name__)

# ── HELPER DI SERIALIZZAZIONE LOCATION ──────────────────────────────────────

def serialize_location(row, columns):
    """
    Trasforma una riga del DB in dizionario JSON.
    """
    loc = dict(zip(columns, row))

    services = loc.get("services")

    if isinstance(services, str):
        try:
            import json
            loc["services"] = json.loads(services)
        except Exception:
            loc["services"] = []
    elif services is None:
        loc["services"] = []

    for field in ("image_est", "image_int", "floor_plan"):
        if loc.get(field) in ("None", ""):
            loc[field] = None

    return loc


# ── ROUTES ───────────────────────────────────────────────────────────────────

@locations_bp.route('/', methods=['GET'], strict_slashes=False)
def get_locations():
    city   = request.args.get('city')
    region = request.args.get('region')
    name   = request.args.get('name')

    locations = LocationService.get_locations(city, region, name=name)

    return jsonify({'locations': locations}), 200


@locations_bp.route('/<int:location_id>', methods=['GET'])
def get_location(location_id):
    try:
        location = LocationService.get_location_detail(location_id)
        return jsonify(location), 200

    except ValueError as e:
        return jsonify({'error': str(e)}), 404


# ── DASHBOARD: POST crea ─────────────────────────────────────────────────────

@locations_bp.route('/', methods=['POST'], strict_slashes=False)
def create_location():
    """Dashboard — crea una nuova location."""
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Body JSON obbligatorio'}), 400

        required = ('name', 'slug', 'city', 'region', 'address')
        missing  = [f for f in required if not data.get(f)]
        if missing:
            return jsonify({'error': f'Campi obbligatori: {", ".join(missing)}'}), 400

        # services: lista → sintassi array PostgreSQL TEXT[] es. '{Bar,WiFi}'
        raw_services = data.get('services') or []
        if isinstance(raw_services, list):
            services_val = ('{' + ','.join(str(s) for s in raw_services) + '}') if raw_services else '{}'
        else:
            services_val = '{}'

        conn = Database().get_connection()

        id_row = conn.run("SELECT COALESCE(MAX(id), 0) + 1 FROM locations")
        new_id = id_row[0][0]

        conn.run(
            """
            INSERT INTO locations
                (id, slug, name, address, city, region,
                 latitude, longitude, capacity, services,
                 link, image_est, image_int, floor_plan)
            VALUES
                (:id, :slug, :name, :address, :city, :region,
                 :latitude, :longitude, :capacity, :services,
                 :link, :image_est, :image_int, :floor_plan)
            """,
            id=new_id,
            slug=data['slug'],
            name=data['name'],
            address=data['address'],
            city=data['city'],
            region=data['region'],
            latitude=data.get('latitude'),
            longitude=data.get('longitude'),
            capacity=data.get('capacity'),
            services=services_val,
            link=data.get('website') or None,
            image_est=data.get('img_exterior') or None,
            image_int=data.get('img_interior') or None,
            floor_plan=data.get('floor_plan_url') or None,
        )
        conn.run("COMMIT")
        return jsonify({'message': 'Location creata', 'id': new_id}), 201
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore nella creazione location'}), 500


# ── DASHBOARD: PUT aggiorna ──────────────────────────────────────────────────

@locations_bp.route('/<int:location_id>', methods=['PUT'])
def update_location(location_id):
    """Dashboard — aggiorna i campi di una location esistente."""
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Body JSON obbligatorio'}), 400

        # Mappa campo HTML → colonna DB
        field_map = {
            'name':          'name',
            'slug':          'slug',
            'address':       'address',
            'city':          'city',
            'region':        'region',
            'latitude':      'latitude',
            'longitude':     'longitude',
            'capacity':      'capacity',
            'website':       'link',
            'img_exterior':  'image_est',
            'img_interior':  'image_int',
            'floor_plan_url': 'floor_plan',
        }

        sets   = []
        params = {'loc_id': location_id}

        for html_key, col in field_map.items():
            if html_key in data:
                pk = f'v_{col}'
                sets.append(f"{col} = :{pk}")
                params[pk] = data[html_key] if data[html_key] != '' else None

        # services: lista → sintassi array PostgreSQL TEXT[] es. '{Bar,WiFi}'
        if 'services' in data:
            raw_s = data['services'] or []
            if isinstance(raw_s, list):
                services_str = ('{' + ','.join(str(s) for s in raw_s) + '}') if raw_s else '{}'
            else:
                services_str = '{}'
            sets.append("services = :v_services")
            params['v_services'] = services_str

        if not sets:
            return jsonify({'error': 'Nessun campo da aggiornare'}), 400

        conn = Database().get_connection()
        conn.run(f"UPDATE locations SET {', '.join(sets)} WHERE id = :loc_id", **params)
        conn.run("COMMIT")
        return jsonify({'message': 'Location aggiornata'}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': "Errore nell'aggiornamento location"}), 500


# ── DASHBOARD: DELETE ────────────────────────────────────────────────────────

@locations_bp.route('/<int:location_id>', methods=['DELETE'])
def delete_location(location_id):
    """Dashboard — elimina una location (CASCADE su eventi e immagini associati)."""
    try:
        conn = Database().get_connection()
        # event_proposals non ha CASCADE: va eliminata prima degli eventi
        conn.run(
            "DELETE FROM event_proposals WHERE event_id IN "
            "(SELECT id FROM events WHERE location_id = :id)",
            id=location_id
        )
        # reviews, favorites, event_reminders, event_images hanno CASCADE → auto
        conn.run("DELETE FROM events WHERE location_id = :id", id=location_id)
        conn.run("DELETE FROM locations WHERE id = :id", id=location_id)
        conn.run("COMMIT")
        return jsonify({'message': 'Location eliminata'}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': "Errore nell'eliminazione location"}), 500


# ── QUERY (usata da script legacy) ───────────────────────────────────────────

LOCATION_QUERY = """
    SELECT
        id, slug, name, address, city, region,
        latitude, longitude, capacity, services,
        link, image_est, image_int, floor_plan
    FROM locations
    ORDER BY name
"""