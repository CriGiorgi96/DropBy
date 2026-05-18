from flask import Blueprint, request, jsonify
from services.location_service import LocationService

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

@locations_bp.route('/', methods=['GET'])
def get_locations():
    city = request.args.get('city')
    region = request.args.get('region')

    locations = LocationService.get_locations(city, region)

    return jsonify({'locations': locations}), 200


@locations_bp.route('/<int:location_id>', methods=['GET'])
def get_location(location_id):
    try:
        location = LocationService.get_location_detail(location_id)
        return jsonify({'location': location}), 200

    except ValueError as e:
        return jsonify({'error': str(e)}), 404


# ── QUERY ────────────────────────────────────────────────────────────────────

LOCATION_QUERY = """
    SELECT
        id,
        slug,
        name,
        address,
        city,
        region,
        latitude,
        longitude,
        capacity,
        services,
        link,
        image_est,
        image_int,
        floor_plan
    FROM locations
    ORDER BY name
"""