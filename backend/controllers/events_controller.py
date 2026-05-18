from flask import Blueprint, request, jsonify
from services.event_service import EventService
import traceback

events_bp = Blueprint('events', __name__)


@events_bp.route('/search', methods=['GET'])
def search_events():
    try:
        city        = request.args.get('city')
        category    = request.args.get('category')
        date        = request.args.get('date')
        query       = request.args.get('query')
        loc_raw     = request.args.get('location_id')
        location_id = int(loc_raw) if loc_raw and loc_raw.isdigit() else None

        events = EventService.search_events(city, category, date, query, location_id)
        return jsonify({'events': events}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore nella ricerca eventi'}), 500


@events_bp.route('/<int:event_id>', methods=['GET'])
def get_event(event_id):
    try:
        event = EventService.get_event_detail(event_id)
        return jsonify({'event': event}), 200
    except ValueError as e:
        return jsonify({'error': str(e)}), 404
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore nel caricamento evento'}), 500
