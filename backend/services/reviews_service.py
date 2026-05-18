from repositories.review_repository import ReviewRepository
from repositories.event_repository import EventRepository

class ReviewsService:
    @staticmethod
    def add_review(user_id, event_id, rating, comment):
        if rating < 1 or rating > 5:
            raise ValueError('Rating tra 1 e 5')
        event = EventRepository.find_by_id(event_id)
        if not event:
            raise ValueError('Evento non trovato')
        existing = ReviewRepository.find_by_user_and_event(user_id, event_id)
        if existing:
            raise ValueError('Hai già recensito questo evento')
        review = ReviewRepository.create({
            'user_id': user_id,
            'event_id': event_id,
            'rating': rating,
            'comment': comment
        })
        return {'message': 'Recensione aggiunta!', 'review': review}

    @staticmethod
    def get_event_reviews(event_id):
        return ReviewRepository.find_by_event(event_id)

    @staticmethod
    def get_user_reviews(user_id):
        return ReviewRepository.find_by_user(user_id)