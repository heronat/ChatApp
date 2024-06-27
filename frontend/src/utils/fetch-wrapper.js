//import { store, authActions } from 'store';

import {authActions, store} from "../store";

export const fetchWrapper = {
    get: request('GET'),
    post: request('POST'),
    put: request('PUT'),
    delete: request('DELETE')
};

//Gestion des requêtes HTTP envoyées au serveur
function request(method) {
    return (url, body) => {
        const requestOptions = {
            method,
            headers: authHeader(url)
        };
        if (body) {
            requestOptions.headers['Content-Type'] = 'application/json';
            requestOptions.body = JSON.stringify(body);
        }
        return fetch(url, requestOptions).then(handleResponse);
    }
}


//Gestion des en-têtes d'authentification, ajoute le token JWT si l'utilisateur est connecté
function authHeader(url) {
    const token = authToken();
    const isLoggedIn = !!token;
    const isApiUrl = url.startsWith(import.meta.env.VITE_BACKEND_API_URL);
    if (isLoggedIn && isApiUrl) {
        return { Authorization: `Bearer ${token}` };
    } else {
        return {};
    }
}

function authToken() {
    return store.getState().auth.user?.accessToken;
}

//Gestion des réponses HTTP retournées par le serveur
function handleResponse(response) {
    return response.text().then(text => {
        const data = text && JSON.parse(text);

        if (!response.ok) {
            if ([401, 403].includes(response.status) && authToken()) {
                // L'utilisateur est automatiquement déconnecté si le serveur retourne une erreur 401 ou 403
                const logout = () => store.dispatch(authActions.forceLogout());
                logout();
            }

            const error = (data && data.message) || response.statusText;
            return Promise.reject(error);
        }

        return data;
    });
}
