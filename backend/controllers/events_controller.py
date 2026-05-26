from flask import Blueprint, request, jsonify
from services.event_service import EventService
from database import Database
from helpers.format_helper import dict_format, dict_format_single
import traceback

events_bp = Blueprint('events', __name__)


# ── DASHBOARD: GET lista ─────────────────────────────────────────────────────

@events_bp.route('/', methods=['GET'], strict_slashes=False)
def get_events():
    """Dashboard — lista eventi con filtri opzionali (title, city, category)."""
    try:
        conn  = Database().get_connection()
        sql   = """
            SELECT
                e.id, e.slug, e.title, e.category, e.city,
                e.event_date   AS date,
                e.start_time   AS time,
                e.duration,    e.price, e.description,
                e.ticket_url,  e.source_url, e.location_id,
                l.name         AS location_name
            FROM events e
            LEFT JOIN locations l ON l.id = e.location_id
            WHERE 1=1
        """
        params   = {}
        title    = request.args.get('title')
        city     = request.args.get('city')
        category = request.args.get('category')

        if title:
            sql += " AND LOWER(e.title) LIKE LOWER(:title)"
            params['title'] = f'%{title}%'
        if city:
            sql += " AND LOWER(e.city) = LOWER(:city)"
            params['city'] = city
        if category:
            sql += " AND LOWER(e.category) = LOWER(:category)"
            params['category'] = category

        sql += " ORDER BY e.event_date DESC"
        rows   = conn.run(sql, **params)
        events = dict_format(conn, rows)
        return jsonify({'events': events}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore nel recupero eventi'}), 500


# ── DASHBOARD: POST crea ─────────────────────────────────────────────────────

@events_bp.route('/', methods=['POST'], strict_slashes=False)
def create_event():
    """Dashboard — crea un nuovo evento."""
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Body JSON obbligatorio'}), 400

        required = ('title', 'slug', 'category', 'date', 'description', 'location_id')
        missing  = [f for f in required if not data.get(f) and data.get(f) != 0]
        if missing:
            return jsonify({'error': f'Campi obbligatori: {", ".join(missing)}'}), 400

        conn = Database().get_connection()

        id_row = conn.run("SELECT COALESCE(MAX(id), 0) + 1 FROM events")
        new_id = id_row[0][0]

        prod_row = conn.run("SELECT id FROM productions LIMIT 1")
        default_prod_id = prod_row[0][0] if prod_row else 1

        loc_row = conn.run("SELECT city FROM locations WHERE id = :id",
                           id=int(data['location_id']))
        if not loc_row:
            return jsonify({'error': 'Location non trovata'}), 404
        city = loc_row[0][0]

        conn.run(
            """
            INSERT INTO events
                (id, slug, production_id, location_id, title, description,
                 category, city, event_date, duration,
                 title_eng, description_eng, info_url_it, info_url_eng)
            VALUES
                (:id, :slug, :production_id, :location_id, :title, :description,
                 :category, :city, :event_date, :duration,
                 :title_eng, :description_eng, :info_url_it, :info_url_eng)
            """,
            id=new_id,
            slug=data['slug'],
            production_id=data.get('production_id') or default_prod_id,
            location_id=int(data['location_id']),
            title=data['title'],
            description=data['description'],
            category=data['category'],
            city=city,
            event_date=data['date'],
            duration=data.get('duration') or None,
            title_eng=data.get('title_eng') or None,
            description_eng=data.get('description_eng') or None,
            info_url_it=data.get('info_url_it') or None,
            info_url_eng=data.get('info_url_eng') or None,
        )
        conn.run("COMMIT")
        return jsonify({'message': 'Evento creato', 'id': new_id}), 201
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore nella creazione evento'}), 500


# ── DASHBOARD: PUT aggiorna ──────────────────────────────────────────────────

@events_bp.route('/<int:event_id>', methods=['PUT'])
def update_event(event_id):
    """Dashboard — aggiorna i campi di un evento esistente."""
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Body JSON obbligatorio'}), 400

        # Mappa campo HTML → colonna DB
        field_map = {
            'title':           'title',
            'slug':            'slug',
            'category':        'category',
            'date':            'event_date',
            'duration':        'duration',
            'description':     'description',
            'location_id':     'location_id',
            'title_eng':       'title_eng',
            'description_eng': 'description_eng',
            'info_url_it':     'info_url_it',
            'info_url_eng':    'info_url_eng',
        }

        sets   = []
        params = {'ev_id': event_id}

        for html_key, col in field_map.items():
            if html_key in data:
                pk = f'v_{col}'
                sets.append(f"{col} = :{pk}")
                params[pk] = data[html_key] if data[html_key] != '' else None

        if not sets:
            return jsonify({'error': 'Nessun campo da aggiornare'}), 400

        conn = Database().get_connection()

        # Aggiorna anche city se cambia location_id
        if 'v_location_id' in params and params['v_location_id']:
            loc_row = conn.run("SELECT city FROM locations WHERE id = :id",
                               id=int(params['v_location_id']))
            if loc_row:
                sets.append("city = :v_city")
                params['v_city'] = loc_row[0][0]

        conn.run(f"UPDATE events SET {', '.join(sets)} WHERE id = :ev_id", **params)
        conn.run("COMMIT")
        return jsonify({'message': 'Evento aggiornato'}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': "Errore nell'aggiornamento evento"}), 500


# ── DASHBOARD: DELETE ────────────────────────────────────────────────────────

@events_bp.route('/<int:event_id>', methods=['DELETE'])
def delete_event(event_id):
    """Dashboard — elimina un evento con pulizia manuale delle tabelle senza CASCADE."""
    try:
        conn = Database().get_connection()
        # event_proposals non ha ON DELETE CASCADE su events(id)
        conn.run("DELETE FROM event_proposals WHERE event_id = :id", id=event_id)
        # reviews, favorites, event_reminders, event_images hanno CASCADE → auto
        conn.run("DELETE FROM events WHERE id = :id", id=event_id)
        conn.run("COMMIT")
        return jsonify({'message': 'Evento eliminato'}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': "Errore nell'eliminazione evento"}), 500


@events_bp.route('/search', methods=['GET'])
def search_events():
    try:
        city          = request.args.get('city')
        category      = request.args.get('category')
        date          = request.args.get('date')
        query         = request.args.get('query')
        loc_raw       = request.args.get('location_id')
        location_id   = int(loc_raw) if loc_raw and loc_raw.isdigit() else None
        only_upcoming = request.args.get('only_upcoming', 'false').lower() == 'true'
        include_past  = request.args.get('include_past',  'false').lower() == 'true'
        start_date    = request.args.get('start_date') or None
        end_date      = request.args.get('end_date') or None

        events = EventService.search_events(
            city, category, date, query, location_id,
            only_upcoming, start_date, end_date, include_past
        )
        return jsonify({'events': events}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore nella ricerca eventi'}), 500


@events_bp.route('/<int:event_id>', methods=['GET'])
def get_event(event_id):
    try:
        include_past = request.args.get('include_past', 'false').lower() == 'true'
        event = EventService.get_event_detail(event_id, include_past=include_past)
        return jsonify({'event': event}), 200
    except ValueError as e:
        return jsonify({'error': str(e)}), 404
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore nel caricamento evento'}), 500
