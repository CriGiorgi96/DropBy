package com.example.dropby.network;

import com.example.dropby.models.Event;
import com.example.dropby.models.EventsResponse;
import com.example.dropby.models.FavoriteItem;
import com.example.dropby.models.Friend;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.models.Location;
import com.example.dropby.models.LocationsResponse;
import com.example.dropby.models.LoginRequest;
import com.example.dropby.models.LoginResponse;
import com.example.dropby.models.Notification;
import com.example.dropby.models.OtpRequest;
import com.example.dropby.models.OtpResponse;
import com.example.dropby.models.Proposal;
import com.example.dropby.models.RegisterRequest;
import com.example.dropby.models.RegisterResponse;
import com.example.dropby.models.Reminder;
import com.example.dropby.models.Review;
import com.example.dropby.models.ReviewRequest;
import com.example.dropby.models.UserSearchResult;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ── EVENTI ───────────────────────────────────────────────────────────────

    @GET("api/events/search")
    Call<EventsResponse> searchEvents(
            @Query("city")          String city,
            @Query("category")      String category,
            @Query("date")          String date,
            @Query("query")         String query,
            @Query("location_id")   Integer locationId,
            @Query("only_upcoming") Boolean onlyUpcoming,
            @Query("start_date")    String startDate,
            @Query("end_date")      String endDate
    );

    @GET("api/events/{eventId}")
    Call<EventWrapper> getEventDetail(@Path("eventId") int eventId);

    // ── LOCATION ─────────────────────────────────────────────────────────────

    @GET("api/locations")
    Call<LocationsResponse> getLocations(
            @Query("city")   String city,
            @Query("region") String region
    );

    @GET("api/locations/{locationId}")
    Call<Location> getLocationById(@Path("locationId") int locationId);

    // ── AUTH ─────────────────────────────────────────────────────────────────

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/auth/verify-otp")
    Call<OtpResponse> verifyOtp(@Body OtpRequest request);

    @POST("api/auth/resend-otp")
    Call<OtpResponse> resendOtp(@Body OtpRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/auth/profile")
    Call<Map<String, Object>> getProfile();

    @GET("api/auth/search")
    Call<UserSearchResult.Response> searchUsers(@Query("q") String query);

    @GET("api/auth/check-username")
    Call<Map<String, Object>> checkUsername(@Query("username") String username);

    @PUT("api/auth/update-profile")
    Call<Map<String, Object>> updateProfile(@Body Map<String, String> body);

    @Multipart
    @POST("api/auth/upload-avatar")
    Call<Map<String, Object>> uploadAvatar(@Part okhttp3.MultipartBody.Part file);

    // ── ACCOUNT ──────────────────────────────────────────────────────────────

    @DELETE("api/users/{userId}")
    Call<GenericResponse> deleteAccount(@Path("userId") int userId);

    // ── PROFILI PUBBLICI UTENTE ───────────────────────────────────────────────

    @GET("api/users/{userId}/profile")
    Call<Map<String, Object>> getUserPublicProfile(@Path("userId") int userId);

    @GET("api/users/{userId}/favorites")
    Call<FavoritesWrapper> getUserFavorites(@Path("userId") int userId);

    @GET("api/users/{userId}/reviews")
    Call<ReviewsWrapper> getUserReviews(@Path("userId") int userId);

    // ── AMICIZIE ─────────────────────────────────────────────────────────────

    @POST("api/friends/request")
    Call<GenericResponse> sendFriendRequest(@Body Map<String, Integer> body);

    @GET("api/friends/requests")
    Call<FriendRequestsWrapper> getPendingFriendRequests();

    @POST("api/friends/respond")
    Call<GenericResponse> respondToFriendRequest(@Body Map<String, Object> body);

    @GET("api/friends/list")
    Call<FriendsWrapper> getFriends();

    // ── RECENSIONI ───────────────────────────────────────────────────────────

    @POST("api/reviews/")
    Call<GenericResponse> addReview(@Body ReviewRequest body);

    @GET("api/reviews/event/{eventId}")
    Call<ReviewsWrapper> getEventReviews(@Path("eventId") int eventId);

    @GET("api/reviews/user")
    Call<ReviewsWrapper> getUserReviews();

    @DELETE("api/reviews/{reviewId}")
    Call<GenericResponse> deleteReview(@Path("reviewId") int reviewId);

    // ── PREFERITI ────────────────────────────────────────────────────────────

    @POST("api/favorites/")
    Call<GenericResponse> addFavorite(@Body Map<String, Integer> body);

    @GET("api/favorites/")
    Call<FavoritesWrapper> getFavorites();

    @DELETE("api/favorites/{eventId}")
    Call<GenericResponse> removeFavorite(@Path("eventId") int eventId);

    // ── NOTIFICHE ────────────────────────────────────────────────────────────

    @GET("api/notifications/")
    Call<NotificationsWrapper> getNotifications(@Query("unread") Boolean unreadOnly);

    @PUT("api/notifications/{id}/read")
    Call<GenericResponse> markNotificationRead(@Path("id") int notifId);

    @DELETE("api/notifications/{id}")
    Call<GenericResponse> deleteNotification(@Path("id") int notifId);

    // ── PROMEMORIA ───────────────────────────────────────────────────────────

    @GET("api/reminders/")
    Call<RemindersWrapper> getReminders();

    @POST("api/reminders/")
    Call<GenericResponse> setReminder(@Body Map<String, Object> body);

    @DELETE("api/reminders/{reminderId}")
    Call<GenericResponse> deleteReminder(@Path("reminderId") int reminderId);

    // ── PROPOSTE ─────────────────────────────────────────────────────────────

    // Body: { "to_user_id": int, "event_id": int }
    @POST("api/proposals/")
    Call<GenericResponse> proposeEvent(@Body Map<String, Integer> body);

    // Body: { "event_id": int, "response_emoji": "👍"|"👎"|"🤔" }
    @POST("api/proposals/respond")
    Call<GenericResponse> respondToProposal(@Body Map<String, Object> body);

    // Proposte inviate dall'utente con response_emoji dell'amico (null se pending)
    @GET("api/proposals/status")
    Call<ProposalsWrapper> getProposalStatus();

    // Proposte ricevute ancora in attesa di risposta
    @GET("api/proposals/received")
    Call<ProposalsWrapper> getReceivedProposals();

    // ── WRAPPER CLASSI (risposte lista) ──────────────────────────────────────

    class EventWrapper {
        @com.google.gson.annotations.SerializedName("event")
        public Event event;
    }

    class FriendsWrapper {
        public List<Friend> friends;
    }

    class FriendRequestsWrapper {
        public List<Friend> requests;
    }

    class ReviewsWrapper {
        public List<Review> reviews;
    }

    class FavoritesWrapper {
        public List<FavoriteItem> favorites;
    }

    class NotificationsWrapper {
        public List<Notification> notifications;
    }

    class ProposalsWrapper {
        public List<Proposal> proposals;
    }

    class RemindersWrapper {
        public List<Reminder> reminders;
    }
}
