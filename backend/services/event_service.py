from repositories.event_repository import EventRepository


class EventService:

    @staticmethod
    def search_events(city=None, category=None, date=None, query=None, location_id=None):
        return EventRepository.search(
            city=city,
            category=category,
            date=date,
            query=query,
            location_id=location_id
        )

    @staticmethod
    def get_event_detail(event_id):
        event = EventRepository.find_by_id(event_id)
        if not event:
            raise ValueError('Evento non trovato')
        return event