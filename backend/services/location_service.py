from repositories.location_repository import LocationRepository


class LocationService:

    @staticmethod
    def get_locations(city=None, region=None, name=None):
        return LocationRepository.find_all(city, region, name=name)

    @staticmethod
    def get_location_detail(location_id):
        loc = LocationRepository.find_by_id(location_id)
        if not loc:
            raise ValueError('Location non trovata')
        # RIMOSSO: loc['images'] = LocationRepository.get_images(location_id)
        # Le immagini ora arrivano direttamente dalla query (image_est, image_int, floor_plan)
        return loc