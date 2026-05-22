from repositories.event_repository import EventRepository


class EventService:

    @staticmethod
    def search_events(city=None, category=None, date=None, query=None, location_id=None,
                      only_upcoming=False, start_date=None, end_date=None):
        return EventRepository.search(
            city=city,
            category=category,
            date=date,
            query=query,
            location_id=location_id,
            only_upcoming=only_upcoming,
            start_date=start_date,
            end_date=end_date,
        )

    @staticmethod
    def get_event_detail(event_id):
        event = EventRepository.find_by_id(event_id)
        if not event:
            raise ValueError('Evento non trovato')
        return event