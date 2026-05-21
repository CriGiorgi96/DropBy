from flask import Blueprint, request, jsonify, current_app
from services.auth_service import AuthService
from utils.decorators import token_required
import traceback
import os
import uuid

auth_bp = Blueprint('auth', __name__)


@auth_bp.route('/register', methods=['POST'])
def register():
    try:
        data = request.get_json()
        required = ('username', 'email', 'password')
        if not data or any(not data.get(f) for f in required):
            return jsonify({'error': 'username, email e password sono obbligatori'}), 400
        if len(data['password']) < 6:
            return jsonify({'error': 'Password minimo 6 caratteri'}), 400
        if '@' not in data['email']:
            return jsonify({'error': 'Email non valida'}), 400

        result = AuthService.register_user(
            username=data['username'],
            email=data['email'],
            password=data['password'],
            first_name=data.get('first_name'),
            last_name=data.get('last_name'),
            birthday=data.get('birthday'),
            gender=data.get('gender')
        )
        return jsonify(result), 201

    except ValueError as e:
        return jsonify({'error': str(e)}), 409
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore del server'}), 500


@auth_bp.route('/verify-otp', methods=['POST'])
def verify_otp():
    """Verifica il codice OTP ricevuto via email."""
    try:
        data = request.get_json()
        if not data or not data.get('user_id') or not data.get('otp_code'):
            return jsonify({'error': 'user_id e otp_code obbligatori'}), 400

        result = AuthService.verify_otp(data['user_id'], data['otp_code'])
        return jsonify(result), 200

    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore del server'}), 500


@auth_bp.route('/resend-otp', methods=['POST'])
def resend_otp():
    """Reinvia l'OTP se scaduto o non ricevuto."""
    try:
        data = request.get_json()
        if not data or not data.get('user_id'):
            return jsonify({'error': 'user_id obbligatorio'}), 400

        result = AuthService.resend_otp(data['user_id'])
        return jsonify(result), 200

    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore del server'}), 500


@auth_bp.route('/login', methods=['POST'])
def login():
    try:
        data = request.get_json()
        if not data or not data.get('email') or not data.get('password'):
            return jsonify({'error': 'Email e password obbligatorie'}), 400

        result = AuthService.login_user(data['email'], data['password'])
        if not result:
            return jsonify({'error': 'Credenziali non valide'}), 401

        return jsonify(result), 200

    except ValueError as e:
        # Email non verificata
        return jsonify({'error': str(e)}), 403
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore del server'}), 500


@auth_bp.route('/profile', methods=['GET'])
@token_required
def get_profile(current_user):
    try:
        profile = AuthService.get_user_profile(current_user['user_id'])
        return jsonify({'user': profile}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Profilo non trovato'}), 404


@auth_bp.route('/search', methods=['GET'])
@token_required
def search_users(current_user):
    try:
        q = request.args.get('q', '').strip()
        if len(q) < 2:
            return jsonify({'users': [], 'message': 'Inserisci almeno 2 caratteri'}), 200
        users = AuthService.search_users(q, exclude_user_id=current_user['user_id'])
        return jsonify({'users': users}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore ricerca utenti'}), 500


@auth_bp.route('/check-username', methods=['GET'])
@token_required
def check_username(current_user):
    try:
        username = request.args.get('username', '').strip()
        if not username:
            return jsonify({'error': 'Username obbligatorio'}), 400
        available = AuthService.check_username_available(username, current_user['user_id'])
        return jsonify({'available': available}), 200
    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore server'}), 500


@auth_bp.route('/update-profile', methods=['PUT'])
@token_required
def update_profile(current_user):
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Body JSON obbligatorio'}), 400
        result = AuthService.update_user_profile(current_user['user_id'], data)
        return jsonify(result), 200
    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore server'}), 500


@auth_bp.route('/upload-avatar', methods=['POST'])
@token_required
def upload_avatar(current_user):
    try:
        if 'avatar' not in request.files:
            return jsonify({'error': 'File avatar obbligatorio'}), 400

        file = request.files['avatar']
        if file.filename == '':
            return jsonify({'error': 'Nessun file selezionato'}), 400

        ext = os.path.splitext(file.filename)[1].lower()
        if ext not in ('.jpg', '.jpeg', '.png', '.webp', '.gif'):
            return jsonify({'error': 'Formato file non supportato'}), 400

        filename = f"avatar_{current_user['user_id']}_{uuid.uuid4().hex[:8]}{ext}"
        avatars_dir = os.path.join(current_app.static_folder, 'images', 'avatars')
        os.makedirs(avatars_dir, exist_ok=True)
        file.save(os.path.join(avatars_dir, filename))

        AuthService.update_user_profile(current_user['user_id'], {'profile_image': filename})

        return jsonify({'filename': filename, 'message': 'Avatar aggiornato'}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore upload avatar'}), 500


@auth_bp.route('/verify-config-password', methods=['POST'])
def verify_config_password():
    try:
        data = request.get_json()
        if not data or not data.get('password'):
            return jsonify({'error': 'Password obbligatoria'}), 400
        valid = AuthService.verify_config_password(data['password'])
        if valid:
            return jsonify({'valid': True}), 200
        return jsonify({'valid': False, 'error': 'Password errata'}), 401
    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore server'}), 500