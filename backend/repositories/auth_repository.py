from database import Database
from helpers.format_helper import dict_format_single


class AuthRepository:

    @staticmethod
    def delete_expired_unverified_users():
        conn = Database().get_connection()

        conn.run("""
            DELETE FROM users
            WHERE is_verified = FALSE
            AND otp_expires_at < NOW() - INTERVAL '1 day'
        """)

        conn.run("COMMIT")

    @staticmethod
    def find_by_email(email):
        conn = Database().get_connection()
        rows = conn.run("SELECT * FROM users WHERE email = :email", email=email)
        return dict_format_single(conn, rows)

    @staticmethod
    def find_by_username(username):
        conn = Database().get_connection()
        rows = conn.run("SELECT * FROM users WHERE username = :u", u=username)
        return dict_format_single(conn, rows)

    @staticmethod
    def find_by_id(user_id):
        conn = Database().get_connection()
        rows = conn.run("SELECT * FROM users WHERE id = :id", id=user_id)
        return dict_format_single(conn, rows)

    @staticmethod
    def create_user(username, email, password_hash,
                    first_name=None, last_name=None,
                    date_of_birth=None, gender=None,
                    is_verified=False, otp_code=None, otp_expires_at=None):
        conn = Database().get_connection()
        full_name = f"{first_name or ''} {last_name or ''}".strip() or username

        # Normalizza date_of_birth in oggetto date se arriva come stringa
        if date_of_birth and isinstance(date_of_birth, str):
            from datetime import date as _date
            try:
                date_of_birth = _date.fromisoformat(date_of_birth)
            except ValueError:
                date_of_birth = None

        rows = conn.run(
            """
            INSERT INTO users
                (username, email, password_hash, first_name, last_name, full_name,
                 date_of_birth, gender, is_verified, otp_code, otp_expires_at)
            VALUES
                (:username, :email, :password_hash, :first_name, :last_name, :full_name,
                 :date_of_birth, :gender, :is_verified, :otp_code, :otp_expires_at)
            RETURNING id
            """,
            username=username,
            email=email,
            password_hash=password_hash,
            first_name=first_name,
            last_name=last_name,
            full_name=full_name,
            date_of_birth=date_of_birth,
            gender=gender,
            is_verified=is_verified,
            otp_code=otp_code,
            otp_expires_at=otp_expires_at
        )
        conn.run("COMMIT")
        return rows[0][0]

    @staticmethod
    def update_unverified_user(
        user_id,
        username,
        password_hash,
        first_name,
        last_name,
        date_of_birth,
        gender,
        otp_code,
        otp_expires_at
    ):
        conn = Database().get_connection()

        conn.run(
            """
            UPDATE users
            SET
                username = :username,
                password_hash = :password_hash,
                first_name = :first_name,
                last_name = :last_name,
                date_of_birth = :date_of_birth,
                gender = :gender,
                otp_code = :otp_code,
                otp_expires_at = :otp_expires_at
            WHERE id = :user_id
            """,
            username=username,
            password_hash=password_hash,
            first_name=first_name,
            last_name=last_name,
            date_of_birth=date_of_birth,
            gender=gender,
            otp_code=otp_code,
            otp_expires_at=otp_expires_at,
            user_id=user_id
        )

        conn.run("COMMIT")

    @staticmethod
    def verify_user(user_id):
        conn = Database().get_connection()
        conn.run(
            """UPDATE users
               SET is_verified=TRUE, otp_code=NULL, otp_expires_at=NULL
               WHERE id=:id""",
            id=user_id
        )
        conn.run("COMMIT")

    @staticmethod
    def update_otp(user_id, otp_code, otp_expires_at):
        conn = Database().get_connection()
        conn.run(
            "UPDATE users SET otp_code=:otp, otp_expires_at=:exp WHERE id=:id",
            otp=otp_code, exp=otp_expires_at, id=user_id
        )
        conn.run("COMMIT")

    @staticmethod
    def search_by_username(query, limit=20):
        from helpers.format_helper import dict_format
        conn = Database().get_connection()
        rows = conn.run(
            "SELECT id, username, email FROM users WHERE username ILIKE :q LIMIT :lim",
            q=f"%{query}%", lim=limit
        )
        return dict_format(conn, rows)