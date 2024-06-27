import {useEffect, useState} from "react";

import { useSelector } from 'react-redux';
import {useNavigate} from 'react-router-dom';


import withReactContent from "sweetalert2-react-content";
import Swal from "sweetalert2";
import {fetchWrapper} from "../utils/fetch-wrapper.js";

import Lottie from "lottie-react";
import logoutHamsterAnnimation from "/public/lotties/logout-hamster.json";

export default function PasswordReset() {
    const [loading, setLoading] = useState(false);
    const [tokenIsSet, setTokenIsSet] = useState(true);
    const [error, setError] = useState(undefined);
    const navigate = useNavigate();

    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        if (!token) {
            setTokenIsSet(false);
        }
    }, []);

    const authUser = useSelector(x => x.auth.user);

    // Afficher une notification en haut à droite de l'écran
    const showSwal = (icon, text, subtitle, onClose) => {
        withReactContent(Swal).fire({
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
            },
            didClose: onClose
        })
    }

    useEffect(() => {
        // redirect to home if already logged in
        if (authUser)  navigate("/");
    }, []);


    // Gérer la soumission du formulaire pour la réinitialisation du mot de passe
    async function handleSubmit(event) {
        event.preventDefault()
        const password = event.target.elements.password.value;
        const cpassword = event.target.elements.cpassword.value;

        if (password !== cpassword) {
            setError("Passwords do not match")
            showSwal("error", "Passwords do not match", "Please make sure the passwords match", () => {});
            return
        }

        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');

        if (!token) {
            setTokenIsSet(false);
            return;
        }


        if (loading) return;
        setLoading(true)
        try {
            let response = await fetchWrapper.post(`${import.meta.env.VITE_BACKEND_API_URL}auth/password/reset/${token}`, {password : password});
            if (response.ok) {
                setLoading(false);
                showSwal("success", "Password Reset Successful", "You can now login with your new password.", () => navigate('/login'));
                return 0;
            }
            showSwal("error", "An error occurred", "An error occurred. Please try again later.");
            setError("An error occurred. Please try again later.");
            setLoading(false);
        } catch (error) {
            console.log(error)
            showSwal("error", error, "An error occurred. Please try again later.");
            setError(error);
            setLoading(false);
        }


        return 0;
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
                        Reset your password
                    </h2>
                </div>

                {
                    !tokenIsSet ? (
                        <div className="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
                            <Lottie animationData={logoutHamsterAnnimation} style={{width: "100%", height: "100%"}}/>
                            <h5 className="mt-10 text-center text-2xl font-bold leading-9 tracking-tight text-gray-900"
                                style={{
                                    fontFamily: 'Comic Sans MS',
                                    fontSize: "18px",
                                    fontWeight: 500,
                                    textAlign: 'center',

                                }}
                            >
                                No token found in the URL. Please check your email for the password reset link.
                            </h5>
                            <button
                                onClick={() => navigate("/login")}
                                className="mt-5 flex w-full justify-center rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                            >
                                Go back to login
                            </button>
                        </div>
                        ) :
                        <div className="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
                            <form className="space-y-6" action="#" method="POST" onSubmit={handleSubmit}>
                                <div>
                                    <div className="flex items-center justify-between">
                                        <label htmlFor="email"
                                               className="block text-sm font-medium leading-6 text-gray-900">
                                            Enter Your New Password
                                        </label>
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

                                <div>
                                    <div className="flex items-center justify-between">
                                        <label htmlFor="password"
                                               className="block text-sm font-medium leading-6 text-gray-900">
                                            Please Confirm Your New Password
                                        </label>
                                    </div>
                                    <div className="mt-2">
                                        <input
                                            id="cpassword"
                                            name="cpassword"
                                            type="password"
                                            autoComplete="current-password"
                                            required
                                            className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                        />
                                    </div>
                                </div>

                                {error && (
                                    <span
                                        className="title"
                                        style={{color: "#f00", fontSize: "0.8em"}}
                                    >
                                {error}
                            </span>
                                )}
                                <div>
                                    <button
                                        type="submit"
                                        className="flex w-full justify-center rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                                    >
                                        {!loading ? "Change My Password!" : "Changing your password .."}
                                    </button>
                                </div>

                                <div className="text-sm">
                                    <a href="/login" className="font-semibold text-indigo-600 hover:text-indigo-500">
                                        I remember my password, get me back to login !
                                    </a>
                                </div>
                            </form>
                        </div>
                }
            </div>
        </>
    )
}
