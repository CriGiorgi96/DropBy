from flask import Flask, jsonify, send_from_directory
from flask_cors import CORS
from database import init_db
from config import Config
import os

app = Flask(__name__, static_folder='static', static_url_path='/static')

# --- GESTIONE ASSET STATICI ---

@app.route('/category-assets/<filename>')
def serve_category_asset(filename):
    path = os.path.join(app.static_folder, 'images', 'img_cat')
    return send_from_directory(path, filename)

@app.route('/assets/events/<filename>')
def serve_event_asset(filename):
    path = os.path.join(app.static_folder, 'images', 'foto_luoghi', 'locandine')
    return send_from_directory(path, filename)

@app.route('/assets/locations/<filename>')
def serve_location_cover_alt(filename):
    path = os.path.join(app.static_folder, 'images', 'foto_luoghi', 'foto_teatri')
    return send_from_directory(path, filename)

@app.route('/static/images/foto_luoghi/<subdir>/<filename>')
def serve_deep_assets(subdir, filename):
    path = os.path.join(app.static_folder, 'images', 'foto_luoghi', subdir)
    return send_from_directory(path, filename)

@app.route('/assets/avatars/<filename>')
def serve_avatar(filename):
    path = os.path.join(app.static_folder, 'images', 'avatars')
    return send_from_directory(path, filename)

# --- CONFIGURAZIONE ---

app.config['SECRET_KEY'] = Config.SECRET_KEY
CORS(app)

init_db(app)

# --- REGISTRAZIONE BLUEPRINT ---

from controllers.auth_controller import auth_bp
app.register_blueprint(auth_bp, url_prefix='/api/auth')

from controllers.parking_controller import parking_bp
app.register_blueprint(parking_bp, url_prefix='/api/parking')

from controllers.locations_controller import locations_bp
app.register_blueprint(locations_bp, url_prefix='/api/locations')

from controllers.events_controller import events_bp
app.register_blueprint(events_bp, url_prefix='/api/events')

from controllers.friends_controller import friends_bp
app.register_blueprint(friends_bp, url_prefix='/api/friends')

from controllers.reviews_controller import reviews_bp
app.register_blueprint(reviews_bp, url_prefix='/api/reviews')

from controllers.favorites_controller import favorites_bp
app.register_blueprint(favorites_bp, url_prefix='/api/favorites')

from controllers.notifications_controller import notifications_bp
app.register_blueprint(notifications_bp, url_prefix='/api/notifications')

from controllers.proposals_controller import proposals_bp
app.register_blueprint(proposals_bp, url_prefix='/api/proposals')

from controllers.reminders_controller import reminders_bp
app.register_blueprint(reminders_bp, url_prefix='/api/reminders')

@app.route('/')
def home():
    return jsonify({
        "status": "online",
        "endpoints": {
            "auth": ["register", "login", "profile"],
            "parking": ["save", "history", "delete"],
            "locations": ["list (city/region)", "detail/<id>"],
            "events": ["search (city, category, date, query)", "detail/<id>"],
            "friends": ["request", "pending", "respond", "list"],
            "reviews": ["add", "for event"],
            "favorites": ["add", "list", "remove"],
            "notifications": ["list", "mark read"],
            "proposals": ["propose", "pending", "respond"],
            "reminders": ["list", "create", "delete/<id>"]
        }
    })

@app.route('/api/health')
def health():
    return jsonify({"status": "healthy"})

if __name__ == '__main__':
    print("🚀 Server avviato con tutti i moduli")
    app.run(host='0.0.0.0', port=5000, debug=True)