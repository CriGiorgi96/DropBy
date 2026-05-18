import json
from database import Database
from helpers.format_helper import dict_format, dict_format_single

class EventRepository:
    @staticmethod
    def search(city=None, category=None, date=None, query=None, location_id=None):
        db = Database()
        conn = db.get_connection()
        # Query che aggrega le immagini per non dover fare query separate
        sql = """
            SELECT e.*, 
                   COALESCE(
                       (SELECT json_agg(json_build_object('url', ei.url, 'alt', ei.alt) ORDER BY ei.image_order)
                        FROM event_images ei WHERE ei.event_id = e.id), '[]'
                   ) AS images
            FROM events e WHERE 1=1
        """
        params = {}
        if city:
            sql += " AND LOWER(e.city) = LOWER(:city)"; params['city'] = city
        if category:
            sql += " AND LOWER(e.category) = LOWER(:cat)"; params['cat'] = category
        if date:
            sql += " AND e.event_date = :date"; params['date'] = date
        if query:
            sql += " AND (LOWER(e.title) LIKE LOWER(:q) OR LOWER(e.description) LIKE LOWER(:q))"
            params['q'] = f"%{query}%"
        if location_id:
            sql += " AND e.location_id = :loc_id"; params['loc_id'] = location_id
        
        sql += " ORDER BY e.event_date DESC"
        rows = conn.run(sql, **params)
        return dict_format(conn, rows)

    @staticmethod
    def find_by_id(event_id):
        db = Database()
        conn = db.get_connection()
        # Query per il dettaglio singolo che include già le immagini nel JSON
        sql = """
            SELECT e.*, 
                   COALESCE(
                       (SELECT json_agg(json_build_object('url', ei.url, 'alt', ei.alt) ORDER BY ei.image_order)
                        FROM event_images ei WHERE ei.event_id = e.id), '[]'
                   ) AS images
            FROM events e WHERE e.id = :id
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