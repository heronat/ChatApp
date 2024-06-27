import {useEffect, useState} from "react";

import { useSelector, useDispatch } from 'react-redux';
import {useNavigate} from 'react-router-dom';


import withReactContent from "sweetalert2-react-content";
import Swal from "sweetalert2";
import {fetchWrapper} from "../utils/fetch-wrapper.js";

export default function RegisterPage() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(undefined);
    const navigate = useNavigate();

    const authUser = useSelector(x => x.auth.user);

    useEffect(() => {
        // redirect to home if already logged in
        if (authUser)  navigate("/");
    }, []);

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

    //récupérer les données du formulaire et les envoyer à l'API pour créer un compte
    async function handleSubmit(event) {
        event.preventDefault()

        const email = event.target.elements.email.value;
        const password = event.target.elements.password.value;
        const cpassword = event.target.elements.cpassword.value;
        const lname = event.target.elements.lname.value;
        const fname = event.target.elements.fname.value;

        if (loading) return;
        setLoading(true)

        if (password !== cpassword) {
            setError("Passwords do not match")
            showSwal("error", "Passwords do not match", "Please make sure the passwords match", () => {});
            setLoading(false)
            return
        }

        try {
            let response = await fetchWrapper.post(`${import.meta.env.VITE_BACKEND_API_URL}auth/signup`,
                {
                        email : email,
                        password : password,
                        firstName : fname,
                        lastName : lname
                }
            );
            setLoading(false);
            showSwal("success", "Account created successfully", "You can now login with your credentials", () => navigate('/login'));
            return 0;
        } catch (error) {
            showSwal("error", error, "An error occurred. Please try again later. Error: " + error);
            setError(error);
            setLoading(false);
        }


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
                        Create an account
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
                                <label htmlFor="fname" className="block text-sm font-medium leading-6 text-gray-900">
                                    First Name
                                </label>
                            </div>
                            <div className="mt-2">
                                <input
                                    id="fname"
                                    name="fname"
                                    type="text"
                                    autoComplete="family-name"
                                    required
                                    className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                />
                            </div>
                        </div>

                        <div>
                            <div className="flex items-center justify-between">
                                <label htmlFor="lname" className="block text-sm font-medium leading-6 text-gray-900">
                                    Last Name
                                </label>
                            </div>
                            <div className="mt-2">
                                <input
                                    id="lname"
                                    name="lname"
                                    type="text"
                                    autoComplete="name"
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
                                <label htmlFor="cpassword" className="block text-sm font-medium leading-6 text-gray-900">
                                    Confirm Password
                                </label>
                            </div>
                            <div className="mt-2">
                                <input
                                    id="cpassword"
                                    name="cpassword"
                                    type="password"
                                    autoComplete="new-password"
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
                                {!loading ? "Register" : "Registering..."}
                            </button>
                        </div>
                        <div className="text-sm">
                            <a href="/login" className="font-semibold text-indigo-600 hover:text-indigo-500">
                                I already have an account, login !
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </>
    )
}
