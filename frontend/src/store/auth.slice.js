import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';

import {fetchWrapper} from "../utils/fetch-wrapper";

//Gérer l'état de l'authentification au sein de l'application
const authSlice = createSlice({
    name: 'auth',
    initialState: () => {
        return {
            user: JSON.parse(localStorage.getItem('user')),
            error: null
        }
    },
    reducers : {
        forceLogout(state) {
            state.user = null;
            localStorage.removeItem('user');
        }
    },
    extraReducers : (builder) => {
        //Mise à jour de l'état de l'authentification en fonction de l'état de la requête
        builder.addCase(authActions.login.fulfilled, (state, action) => {
            const user = action.payload;
            localStorage.setItem('user', JSON.stringify(user));
            state.user = user;
        });
        builder.addCase(authActions.login.rejected, (state, action) => {
            state.error = action.error;
        });
        builder.addCase(authActions.login.pending, (state) => {
            state.error = null;
        });
        builder.addCase(authActions.logout.fulfilled, (state) => {
            state.user = null;
            localStorage.removeItem('user');
        });
        builder.addCase(authActions.logout.rejected, (state, action) => {
            state.error = action.error;
        });
        builder.addCase(authActions.logout.pending, (state) => {
            state.error = null;
        });
    }
});

// Fonction pour créer des actions supplémentaires liées à l'authentification
function createExtraActions() {
    const baseUrl = `${import.meta.env.VITE_BACKEND_API_URL}auth`;

    return {
        login: login(),
        logout: logout()
    };

    function login() {
        return createAsyncThunk(
            `auth/login`,
            async ({ email, password }) => await fetchWrapper.post(`${baseUrl}/signin`, { email, password })
        );
    }

    function logout() {
        return createAsyncThunk(
            `auth/logout`,
            async ({ email, token }) => await fetchWrapper.post(`${baseUrl}/logout`, { email, token })
        );
    }
}
const extraActions = createExtraActions();

export const authActions = { ...authSlice.actions, ...extraActions };
export const authReducer = authSlice.reducer;

