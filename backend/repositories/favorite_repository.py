from database import Database
from helpers.format_helper import dict_format, dict_format_single

class FavoriteRepository:
    @staticmethod
    def add(user_id, event_id):
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            "INSERT INTO favorites (user_id, event_id) VALUES (:user_id, :event_id) "
            "ON CONFLICT DO NOTHING RETURNING *",
            user_id=user_id, event_id=event_id
        )
        # dict_format PRIMA di COMMIT: conn.columns viene azzerato da COMMIT
        result = dict_format_single(conn, rows)
        conn.run("COMMIT")
        return result

    @staticmethod
    def find_by_user(user_id):
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            """SELECT f.id AS favorite_id, f.created_at, f.event_id,
                      e.title, e.category, e.city, e.event_date, e.price
               FROM favorites f
               JOIN events e ON f.event_id = e.id
               WHERE f.user_id = :user_id
               ORDER BY f.created_at DESC""",
            user_id=user_id
        )
        return dict_format(conn, rows)

    @staticmethod
    def remove(user_id, event_id):
        db = Database()
        conn = db.get_connection()
        conn.run(
            "DELETE FROM favorites WHERE user_id = :user_id AND event_id = :event_id",
            user_id=user_id, event_id=event_id
        )
        conn.run("COMMIT")

    @staticmethod
    def is_favorite(user_id, event_id):
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            "SELECT 1 FROM favorites WHERE user_id = :user_id AND event_id = :event_id",
            user_id=user_id, event_id=event_id
        )
        return len(rows) > 0
