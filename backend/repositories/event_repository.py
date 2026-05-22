import json
from database import Database
from helpers.format_helper import dict_format, dict_format_single

class EventRepository:
    @staticmethod
    def search(city=None, category=None, date=None, query=None, location_id=None,
               only_upcoming=False, start_date=None, end_date=None):
        db = Database()
        conn = db.get_connection()
        sql = """
            SELECT e.*,
                   l.name      AS location_name,
                   l.image_est AS location_image_est,
                   l.image_int AS location_image_int,
                   COALESCE(
                       (SELECT json_agg(json_build_object('url', ei.url, 'alt', ei.alt) ORDER BY ei.image_order)
                        FROM event_images ei WHERE ei.event_id = e.id), '[]'
                   ) AS images,
                   ROUND(COALESCE(
                       (SELECT AVG(r.rating)::numeric FROM reviews r WHERE r.event_id = e.id), 0
                   ), 1) AS avg_rating,
                   (SELECT COUNT(*) FROM reviews r WHERE r.event_id = e.id) AS review_count
            FROM events e
            LEFT JOIN locations l ON l.id = e.location_id
            WHERE 1=1
        """
        params = {}
        if only_upcoming:
            sql += " AND e.event_date >= CURRENT_DATE AND e.location_id IS NOT NULL"
        if city:
            sql += " AND LOWER(e.city) = LOWER(:city)"; params['city'] = city
        if category:
            sql += " AND LOWER(e.category) = LOWER(:cat)"; params['cat'] = category
        if date:
            sql += " AND e.event_date = :date"; params['date'] = date
        if start_date:
            sql += " AND e.event_date >= :start_date"; params['start_date'] = start_date
        if end_date:
            sql += " AND e.event_date <= :end_date"; params['end_date'] = end_date
        if query:
            sql += " AND (LOWER(e.title) LIKE LOWER(:q) OR LOWER(e.description) LIKE LOWER(:q))"
            params['q'] = f"%{query}%"
        if location_id:
            sql += " AND e.location_id = :loc_id"; params['loc_id'] = location_id

        sql += " ORDER BY e.event_date " + ("ASC" if only_upcoming else "DESC")
        rows = conn.run(sql, **params)
        return dict_format(conn, rows)

    @staticmethod
    def find_by_id(event_id):
        db = Database()
        conn = db.get_connection()
        # Query per il dettaglio singolo che include già le immagini nel JSON
        sql = """
            SELECT e.*,
                   l.name      AS location_name,
                   l.image_est AS location_image_est,
                   l.image_int AS location_image_int,
                   COALESCE(
                       (SELECT json_agg(json_build_object('url', ei.url, 'alt', ei.alt) ORDER BY ei.image_order)
                        FROM event_images ei WHERE ei.event_id = e.id), '[]'
                   ) AS images,
                   ROUND(COALESCE(
                       (SELECT AVG(r.rating)::numeric FROM reviews r WHERE r.event_id = e.id), 0
                   ), 1) AS avg_rating,
                   (SELECT COUNT(*) FROM reviews r WHERE r.event_id = e.id) AS review_count
            FROM events e
            LEFT JOIN locations l ON l.id = e.location_id
            WHERE e.id = :id
        """
        rows = conn.run(sql, id=event_id)
        return dict_format_single(conn, rows)

    @staticmethod
    def get_images(event_id):
        db = Database()
        conn = db.get_connection()
        # Metodo di supporto per recuperare solo le immagini se necessario
        return conn.run(
            "SELECT * FROM event_images WHERE event_id = :id ORDER BY image_order",
            id=event_id
        )