package com.example.dropby.network; // Definisce il package della classe di compatibilità per il networking.

public final class RetrofitClient { // Mantiene il vecchio nome come wrapper per non rompere eventuali chiamate esistenti.

    private RetrofitClient() { // Impedisce l'istanziazione perché la classe delega solo a metodi statici.
    } // Chiude il costruttore privato.

    public static ApiService getClient() { // Restituisce il servizio API tipizzato atteso da chi usa il vecchio wrapper.
        return ApiClient.getApiService(); // Delega al client unico configurato con 10.0.2.2, timeout e Gson.
    } // Chiude il metodo di compatibilità.
} // Chiude la classe RetrofitClient.
