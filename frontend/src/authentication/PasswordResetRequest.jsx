import {useEffect, useState} from "react";

import { useSelector, useDispatch } from 'react-redux';
import {useNavigate} from 'react-router-dom';


import withReactContent from "sweetalert2-react-content";
import Swal from "sweetalert2";
import {fetchWrapper} from "../utils/fetch-wrapper.js";

export default function PasswordResetRequest() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(undefined);
    const navigate = useNavigate();

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


    //requête API pour envoyer un mail pour réinitialiser le mot de passe
    async function handleSubmit(event) {
        event.preventDefault()
        const email = event.target.elements.email.value;

        if (loading) return;
        setLoading(true)
        try {
            let response = await fetchWrapper.post(`${import.meta.env.VITE_BACKEND_API_URL}auth/password/forgotten`, {email : email});
            if (response.ok) {
                setLoading(false);
                showSwal("success", "Email Sent", "An email has been sent to your email address with instructions on how to reset your password.", () => {navigate("/login"); });
                return 0;
            }
            console.log(response)
            showSwal("error", "An error occurred", "An error occurred. Please try again later.");
            setError("An error occurred. Please try again later.");
            setLoading(false);
        } catch (error) {
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
                                {!loading ? "Send email" : "Sending Email.."}
                            </button>
                        </div>

                        <div className="text-sm">
                            <a href="/login" className="font-semibold text-indigo-600 hover:text-indigo-500">
                                I remember my password, get me back to login !
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </>
    )
}
