from database import Database
from helpers.format_helper import dict_format, dict_format_single


class LocationRepository:

    # Colonne esplicite: niente più l.* per evitare conflitti di nome
    # tra colonne della tabella e alias delle subquery.
    _BASE_SELECT = """
        SELECT
            l.id,
            l.slug,
            l.name,
            l.address,
            l.city,
            l.region,
            l.latitude,
            l.longitude,
            l.capacity,
            l.services,
            l.link,
            l.image_est,
            l.image_int,
            l.floor_plan
        FROM locations l
        WHERE 1=1
    """

    @staticmethod
    def find_all(city=None, region=None):
        db   = Database()
        conn = db.get_connection()

        query  = LocationRepository._BASE_SELECT
        params = {}

        if city:
            query += " AND LOWER(l.city) = LOWER(:city)"
            params['city'] = city
        if region:
            query += " AND LOWER(l.region) = LOWER(:region)"
            params['region'] = region

        query += " ORDER BY l.name"

        rows = conn.run(query, **params)
        return dict_format(conn, rows)

    @staticmethod
    def find_by_id(location_id):
        db   = Database()
        conn = db.get_connection()

        query = LocationRepository._BASE_SELECT + " AND l.id = :id"
        rows  = conn.run(query, id=location_id)
        return dict_format_single(conn, rows)