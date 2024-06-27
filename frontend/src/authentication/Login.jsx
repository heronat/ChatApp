import {useEffect, useState} from "react";

import { useSelector, useDispatch } from 'react-redux';
import {useNavigate} from 'react-router-dom';


import { authActions } from '../store';
import withReactContent from "sweetalert2-react-content";
import Swal from "sweetalert2";

export default function LoginPage() {
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const authUser = useSelector(x => x.auth.user);
    const authError = useSelector(x => x.auth.error);

    // Afficher une notification en haut à droite de l'écran
    const showSwal = (icon, text, subtitle) => {
        return withReactContent(Swal).fire({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true,
            icon: icon,
            title: text,
            text: subtitle,
            didOpen: (toast) => {
                toast.onmouseenter = Swal.stopTimer;
                toast.onmouseleave = Swal.resumeTimer;
            }
        })
    }

    useEffect(() => {
        // redirect to home if already logged in
        if (authUser)  navigate("/");
    }, []);

    useEffect(() => {
        if (authUser) {
            showSwal(
                "success",
                "Authentication Successful",
                "Redirecting ..").then(
                () => window.location = "/"
            );
        }
    }, [authUser]);

    useEffect(() => {
        if (authError) {
            setLoading(false)
            showSwal("error", authError.message, "An error occurred. Please try again.")
        }
    }, [authError]);

    // Gérer la soumission du formulaire
    async function handleSubmit(event) {
        event.preventDefault()
        if (loading) return;
        const email = event.target.elements.email.value;
        const password = event.target.elements.password.value;
        setLoading(true)
        return dispatch(authActions.login({ email, password }));
    }


    return (
        <>
            <div className="flex items-center min-h-screen flex-1 flex-col justify-center px-6 py-12 lg:px-8">
                <div className="sm:mx-auto sm:w-full sm:max-w-sm">
                    <h1
                        style={{
                            fontFamily: 'Pacifico',
                            fontSize: "68px",
                            fontWeight: 500,
                            textAlign: 'center',
                        }}
                    >
                        ChatApp
                    </h1>
                    <h2 className="mt-10 text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">
                        Sign in to your account
                    </h2>
                </div>

                <div className="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
                    <form className="space-y-6" action="#" method="POST" onSubmit={handleSubmit}>
                        <div>
                            <div className="flex items-center justify-between">
                                <label htmlFor="email" className="block text-sm font-medium leading-6 text-gray-900">
                                    Email address
                                </label>
                            </div>
                            <div className="mt-2">
                                <input
                                    id="email"
                                    name="email"
                                    type="email"
                                    autoComplete="email"
                                    required
                                    className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                />
                            </div>
                        </div>

                        <div>
                            <div className="flex items-center justify-between">
                                <label htmlFor="password" className="block text-sm font-medium leading-6 text-gray-900">
                                    Password
                                </label>
                                <div className="text-sm">
                                    <a href="/password-reset-request" className="font-semibold text-indigo-600 hover:text-indigo-500">
                                        Forgot password?
                                    </a>
                                </div>
                            </div>
                            <div className="mt-2">
                                <input
                                    id="password"
                                    name="password"
                                    type="password"
                                    autoComplete="current-password"
                                    required
                                    className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                />
                            </div>
                        </div>
                        {authError && (
                            <span
                                className="title"
                                style={{color: "#f00", fontSize: "0.8em"}}
                            >
                                {authError.message}
                            </span>
                        )}
                        <div>
                            <button
                                type="submit"
                                className="flex w-full justify-center rounded-md bg-gray-100 px-3 py-1.5 text-sm font-semibold leading-6 text-black shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                            >
                                {!loading ? "Login" : "Logging In.."}
                            </button>
                        </div>

                        <div>
                            <button
                                type="button"
                                onClick={
                                    () => navigate("/register")
                                }
                                className="flex w-full justify-center rounded-md bg-gray-100 px-3 py-1.5 text-sm font-semibold leading-6 text-black shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                            >
                                Don't have an account? Register !
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </>
    )
}

