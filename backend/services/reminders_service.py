from datetime import datetime, timezone
from repositories.notification_repository import NotificationRepository
from repositories.event_repository import EventRepository

class RemindersService:
    @staticmethod
    def get_reminders(user_id):
        return NotificationRepository.find_reminders_by_user(user_id)

    @staticmethod
    def create_reminder(user_id, event_id, remind_at_str):
        event = EventRepository.find_by_id(event_id)
        if not event:
            raise ValueError('Evento non trovato')

        try:
            remind_at = datetime.fromisoformat(remind_at_str.replace('Z', '+00:00'))
            if remind_at.tzinfo is None:
                remind_at = remind_at.replace(tzinfo=timezone.utc)
        except (ValueError, AttributeError):
            raise ValueError('Formato data non valido — usa ISO 8601 (es. 2025-06-01T18:00:00)')

        if remind_at <= datetime.now(timezone.utc):
            raise ValueError('La data del promemoria deve essere nel futuro')

        # Il promemoria non può essere dopo la data dell'evento
        event_date_raw = event.get('event_date')
        if event_date_raw:
            try:
                date_str = str(event_date_raw)[:10]
                from datetime import timedelta
                deadline = datetime(*[int(x) for x in date_str.split('-')],
                                    tzinfo=timezone.utc) + timedelta(days=1)
                if remind_at > deadline:
                    raise ValueError("Il promemoria deve essere impostato entro la data dell'evento")
            except (ValueError, TypeError):
                pass

        reminder_id = NotificationRepository.create_reminder(user_id, event_id, remind_at)
        return {'message': 'Promemoria impostato!', 'reminder_id': reminder_id}

    @staticmethod
    def delete_reminder(user_id, reminder_id):
        NotificationRepository.delete_reminder_by_id(user_id, reminder_id)
        return {'message': 'Promemoria rimosso'}
