package com.example.dropby.fragments.main;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dropby.R;
import com.example.dropby.adapters.FriendAdapter;
import com.example.dropby.models.Friend;
import com.example.dropby.models.GenericResponse;
import com.example.dropby.models.UserSearchResult;
import com.example.dropby.network.ApiClient;
import com.example.dropby.network.ApiService;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Nota: Friend.java non ha costruttore pubblico utile qui - si usa solo per i dati dal backend

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsFragment extends Fragment {

    // Tabs
    private static final int TAB_FRIENDS  = 0;
    private static final int TAB_REQUESTS = 1;
    private static final int TAB_SEARCH   = 2;

    private RecyclerView recyclerView;
    private TextView statusText;
    private EditText searchInput;
    private FriendAdapter adapter;

    private int currentTab = TAB_FRIENDS;

    public FriendsFragment() { super(R.layout.fragment_friends); }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.friendsRecyclerView);
        statusText   = view.findViewById(R.id.friendsStatusText);
        searchInput  = view.findViewById(R.id.searchUsersInput);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
<<<<<<< HEAD
        adapter = new FriendAdapter(new ArrayList<>(), this::onFriendAction, this::openUserProfile);
=======
        adapter = new FriendAdapter(new ArrayList<>(), this::onFriendAction);
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        recyclerView.setAdapter(adapter);

        TabLayout tabs = view.findViewById(R.id.friendsTabLayout);
        tabs.addTab(tabs.newTab().setText(R.string.friends_tab_list));
        tabs.addTab(tabs.newTab().setText(R.string.friends_tab_requests));
        tabs.addTab(tabs.newTab().setText(R.string.friends_tab_search));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadCurrentTab();
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });

        view.findViewById(R.id.searchUsersButton).setOnClickListener(v -> executeSearch());
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { executeSearch(); return true; }
            return false;
        });

        loadFriends();
    }

    private void loadCurrentTab() {
        switch (currentTab) {
            case TAB_FRIENDS:  loadFriends();  break;
            case TAB_REQUESTS: loadRequests(); break;
            case TAB_SEARCH:   break;
        }
    }

    private void loadFriends() {
<<<<<<< HEAD
        setStatus(getString(R.string.loading_friends));;
=======
        setStatus("Caricamento amici…");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        ApiClient.getApiService().getFriends().enqueue(new Callback<ApiService.FriendsWrapper>() {
            @Override
            public void onResponse(Call<ApiService.FriendsWrapper> call,
                                   Response<ApiService.FriendsWrapper> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().friends != null) {
                    showFriends(response.body().friends, FriendAdapter.Mode.FRIENDS);
                } else {
                    setStatus(getString(R.string.no_friends));
                }
            }
            @Override
            public void onFailure(Call<ApiService.FriendsWrapper> call, Throwable t) {
                if (isAdded()) setStatus("Errore di rete");
            }
        });
    }

    private void loadRequests() {
<<<<<<< HEAD
        setStatus(getString(R.string.loading_requests));;
=======
        setStatus("Caricamento richieste…");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        ApiClient.getApiService().getPendingFriendRequests()
                .enqueue(new Callback<ApiService.FriendRequestsWrapper>() {
                    @Override
                    public void onResponse(Call<ApiService.FriendRequestsWrapper> call,
                                           Response<ApiService.FriendRequestsWrapper> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().requests != null) {
                            showFriends(response.body().requests, FriendAdapter.Mode.REQUESTS);
                        } else {
                            setStatus(getString(R.string.no_requests));
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiService.FriendRequestsWrapper> call, Throwable t) {
<<<<<<< HEAD
                        if (isAdded()) setStatus(getString(R.string.network_error));
=======
                        if (isAdded()) setStatus("Errore di rete");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                    }
                });
    }

    private void executeSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.length() < 2) {
<<<<<<< HEAD
            Toast.makeText(requireContext(), getString(R.string.insert_at_least_two_chars), Toast.LENGTH_SHORT).show();
            return;
        }
        setStatus(getString(R.string.searching));
=======
            Toast.makeText(requireContext(), "Inserisci almeno 2 caratteri", Toast.LENGTH_SHORT).show();
            return;
        }
        setStatus("Ricerca…");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
        ApiClient.getApiService().searchUsers(query)
                .enqueue(new Callback<UserSearchResult.Response>() {
                    @Override
                    public void onResponse(Call<UserSearchResult.Response> call,
                                           Response<UserSearchResult.Response> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<UserSearchResult> results = response.body().getUsers();
                            if (results == null || results.isEmpty()) {
<<<<<<< HEAD
                                setStatus(getString(R.string.no_user_found));
=======
                                setStatus("Nessun utente trovato");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                                return;
                            }
                            showFriendsFromSearch(results);
                        } else {
<<<<<<< HEAD
                            setStatus(getString(R.string.search_error));
=======
                            setStatus("Errore ricerca");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                        }
                    }
                    @Override
                    public void onFailure(Call<UserSearchResult.Response> call, Throwable t) {
<<<<<<< HEAD
                        if (isAdded()) setStatus(getString(R.string.network_error));
=======
                        if (isAdded()) setStatus("Errore di rete");
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                    }
                });
    }

    private void showFriends(List<Friend> list, FriendAdapter.Mode mode) {
        if (list.isEmpty()) {
            setStatus(mode == FriendAdapter.Mode.REQUESTS
                    ? getString(R.string.no_requests) : getString(R.string.no_friends));
            return;
        }
        statusText.setVisibility(View.GONE);
        adapter.update(list, mode);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showFriendsFromSearch(List<UserSearchResult> results) {
        statusText.setVisibility(View.GONE);
        adapter.updateFromSearch(results);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void onFriendAction(FriendAdapter.Action action, Object item) {
        if (action == FriendAdapter.Action.ACCEPT || action == FriendAdapter.Action.REJECT) {
            Friend f = (Friend) item;
            Map<String, Object> body = new HashMap<>();
            body.put("request_id", f.getFriendshipId());
            body.put("response", action == FriendAdapter.Action.ACCEPT ? "accepted" : "rejected");
            ApiClient.getApiService().respondToFriendRequest(body)
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call,
                                               Response<GenericResponse> response) {
                            if (isAdded()) loadRequests();
                        }
                        @Override public void onFailure(Call<GenericResponse> call, Throwable t) {}
                    });
        } else if (action == FriendAdapter.Action.SEND_REQUEST) {
            UserSearchResult u = (UserSearchResult) item;
            Map<String, Integer> body = Collections.singletonMap("friend_id", u.getId());
            ApiClient.getApiService().sendFriendRequest(body)
                    .enqueue(new Callback<GenericResponse>() {
                        @Override
                        public void onResponse(Call<GenericResponse> call,
                                               Response<GenericResponse> response) {
                            if (!isAdded()) return;
                            String msg = (response.isSuccessful() && response.body() != null)
                                    ? response.body().getMessage()
<<<<<<< HEAD
                                    : getString(R.string.error_sending_request);
=======
                                    : "Errore invio richiesta";
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onFailure(Call<GenericResponse> call, Throwable t) {}
                    });
        }
    }

<<<<<<< HEAD
    private void openUserProfile(int userId) {
        if (!isAdded()) return;
        Bundle bundle = new Bundle();
        bundle.putInt("user_id", userId);
        UserDetailsFragment udf = new UserDetailsFragment();
        udf.setArguments(bundle);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, udf)
                .addToBackStack(null)
                .commit();
    }

=======
>>>>>>> 7712ff0 (aggiunti endpoint x i punti)
    private void setStatus(String msg) {
        recyclerView.setVisibility(View.GONE);
        statusText.setText(msg);
        statusText.setVisibility(View.VISIBLE);
    }
}
