from flask import Blueprint, jsonify
from utils.decorators import token_required
from repositories.auth_repository import AuthRepository
from repositories.favorite_repository import FavoriteRepository
from repositories.review_repository import ReviewRepository
import traceback

users_bp = Blueprint('users', __name__)


@users_bp.route('/<int:user_id>/profile', methods=['GET'])
@token_required
def get_user_public_profile(current_user, user_id):
    try:
        user = AuthRepository.find_by_id(user_id)
        if not user:
            return jsonify({'error': 'Utente non trovato'}), 404
        return jsonify({
            'user_id':          user['id'],
            'username':         user['username'],
            'profile_image':    user.get('profile_image'),
            'private_favorites': bool(user.get('private_favorites') or False),
            'private_reviews':   bool(user.get('private_reviews')   or False)
        }), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore server'}), 500


@users_bp.route('/<int:user_id>/favorites', methods=['GET'])
@token_required
def get_user_favorites(current_user, user_id):
    try:
        user = AuthRepository.find_by_id(user_id)
        if not user:
            return jsonify({'error': 'Utente non trovato'}), 404

        if user.get('private_favorites') and current_user['user_id'] != user_id:
            return jsonify({'private': True, 'favorites': []}), 403

        favorites = FavoriteRepository.find_by_user(user_id)
        return jsonify({'favorites': favorites}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore server'}), 500


@users_bp.route('/<int:user_id>/reviews', methods=['GET'])
@token_required
def get_user_reviews(current_user, user_id):
    try:
        user = AuthRepository.find_by_id(user_id)
        if not user:
            return jsonify({'error': 'Utente non trovato'}), 404

        if user.get('private_reviews') and current_user['user_id'] != user_id:
            return jsonify({'private': True, 'reviews': []}), 403

        reviews = ReviewRepository.find_by_user(user_id)
        return jsonify({'reviews': reviews}), 200
    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'Errore server'}), 500
