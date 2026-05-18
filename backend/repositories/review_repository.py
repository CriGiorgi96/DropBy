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
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            """SELECT r.*, u.username FROM reviews r
               JOIN users u ON r.user_id = u.id
               WHERE r.event_id = :event_id ORDER BY r.created_at DESC""",
            event_id=event_id
        )
        return dict_format(conn, rows)

    @staticmethod
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
    def find_by_user_and_event(user_id, event_id):
        db = Database()
        conn = db.get_connection()
        rows = conn.run(
            "SELECT id FROM reviews WHERE user_id = :user_id AND event_id = :event_id",
            user_id=user_id, event_id=event_id
        )
        return dict_format_single(conn, rows)
