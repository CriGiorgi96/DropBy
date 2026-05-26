from database import Database
from helpers.format_helper import dict_format, dict_format_single

class ReviewRepository:
    @staticmethod
    def create(review_data):
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            "INSERT INTO reviews (user_id, event_id, rating, comment) "
            "VALUES (:user_id, :event_id, :rating, :comment) RETURNING *",
            **review_data
        )
        # dict_format PRIMA di COMMIT: conn.columns viene azzerato da COMMIT
        result = dict_format_single(conn, rows)
        conn.run("COMMIT")
        return result

    @staticmethod
    def find_by_event(event_id):
        db   = Database()
        conn = db.get_connection()

        # 1. Recupera production_id dell'evento richiesto
        ref_rows = conn.run(
            "SELECT production_id FROM events WHERE id = :id", id=event_id
        )
        ref_data = dict_format_single(conn, ref_rows)
        prod_id  = (ref_data.get('production_id') or 0) if ref_data else 0

        # 2. Carica recensioni per tutte le repliche della stessa produzione
        #    (o solo questo evento se production_id è assente)
        _REVIEW_SELECT = """
            SELECT r.*, u.username, u.profile_image,
                   e.event_date, e.city AS event_city,
                   l.name AS location_name
            FROM reviews r
            JOIN users u  ON r.user_id  = u.id
            JOIN events e ON r.event_id = e.id
            LEFT JOIN locations l ON l.id = e.location_id
        """
        if prod_id > 0:
            rows = conn.run(
                _REVIEW_SELECT + "WHERE e.production_id = :prod_id ORDER BY r.created_at DESC",
                prod_id=prod_id
            )
        else:
            rows = conn.run(
                _REVIEW_SELECT + "WHERE r.event_id = :event_id ORDER BY r.created_at DESC",
                event_id=event_id
            )
        return dict_format(conn, rows)

    @staticmethod
    def find_by_user(user_id):
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            """SELECT r.id, r.user_id, r.rating, r.comment, r.created_at,
                      e.id AS event_id, e.title AS event_title,
                      e.event_date, e.city AS event_city,
                      u.username, u.profile_image
               FROM reviews r
               JOIN events e ON r.event_id = e.id
               JOIN users u  ON r.user_id  = u.id
               WHERE r.user_id = :user_id
               ORDER BY r.created_at DESC""",
            user_id=user_id
        )
        return dict_format(conn, rows)

    @staticmethod
<<<<<<< HEAD
=======
    def find_by_user(user_id):
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            """SELECT r.id, r.rating, r.comment, r.created_at,
                      e.id AS event_id, e.title AS event_title,
                      e.event_date, e.city AS event_city
               FROM reviews r
               JOIN events e ON r.event_id = e.id
               WHERE r.user_id = :user_id
               ORDER BY r.created_at DESC""",
            user_id=user_id
        )
        return dict_format(conn, rows)

    @staticmethod
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
    def find_by_user_and_event(user_id, event_id):
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            "SELECT id FROM reviews WHERE user_id = :user_id AND event_id = :event_id",
            user_id=user_id, event_id=event_id
        )
        return dict_format_single(conn, rows)
<<<<<<< HEAD

    @staticmethod
    def delete(review_id, user_id):
        db = Database()
        conn = db.get_connection()
        conn.run(
            "DELETE FROM reviews WHERE id = :id AND user_id = :uid",
            id=review_id, uid=user_id
        )
        conn.run("COMMIT")
=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
