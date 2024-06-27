import React, {useEffect, useState} from 'react';
import {useLocation, useNavigate, useSearchParams} from "react-router-dom";
import {fetchWrapper} from "../utils/fetch-wrapper.js";

import Swal from 'sweetalert2'
import withReactContent from 'sweetalert2-react-content'
import {ArrowRightIcon} from "@heroicons/react/24/outline/index.js";
import {NoSymbolIcon} from "@heroicons/react/24/solid/index.js";

const ManageChat = () => {
    const [chatToEdit, setChatToEdit] = useState(undefined);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [validityDate, setValidityDate] = useState('');

    const [loading, setLoading] = useState(false);
    const [bigLoader, setBigLoader] = useState(false);
    const [isEditPage, setEditPage] = useState(false);
    const [error, setError] = useState(undefined);

    const navigate = useNavigate();
    const location = useLocation();

    const [joinedUsers, setJoinedUsers] = useState([]);

    const [searchParams, setSearchParams] = useSearchParams();

    // Fonction pour afficher une notification Toast avec SweetAlert
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

    //A implémenter: Fonction pour bannir un utilisateur du chat
    const handleKick = async (user) => {

    }

    //Charger les informations du canal si on est sur la page d'édition du canal
    useEffect(
        () => {
            if (!location.pathname.includes("edit")) return;
            let canal = searchParams.get("canal");
            setEditPage(true)
            if (location.pathname.includes("edit") && !!!canal) {
                console.log(canal)
                showSwal("error", "Invalid Chat ID", "Please provide a valid chat ID", (_) => navigate("/"));
                return;
            }
            setBigLoader(true);
            fetchWrapper.get(`${import.meta.env.VITE_BACKEND_API_URL}canals/getCanalInfo?canal=${canal}`)
                .then(
                    response => {
                        console.log(response)
                        if (response.ok) {
                            setName(response.canal.canal_title);
                            setDescription(response.canal.canal_description);
                            setValidityDate(new Date(response.canal.valid_until).toISOString().split('T')[0]);
                            setChatToEdit(response.canal);

                            setJoinedUsers(
                                response.canal.joined_users.map((user) => {
                                    return {
                                        uid: user.uid,
                                        first_name: user.firstName,
                                        last_name: user.lastName,
                                        email: user.email,
                                        is_user_enabled: user.userEnabled
                                    }
                                })
                            );
                        } else {
                            showSwal("error", "Invalid Chat ID", "Please provide a valid chat ID", (_) => navigate("/"));
                        }
                        setBigLoader(false);
                    }
                )

        }, [location]
    )

    // Initialiser la date de validité si ce n'est pas une page d'édition
    useEffect(() => {
        if (isEditPage) return;
        let date = new Date();
        date.setMonth(date.getMonth() + 3);
        setValidityDate(date.toISOString().split('T')[0]);
    }, []);


    // Fonction pour soumettre le formulaire de création ou d'édition du canal
    const handleSubmit = async (e) => {
        setLoading(true)
        e.preventDefault();

        try {
            let backendUrl = isEditPage ? `${import.meta.env.VITE_BACKEND_API_URL}canals/edit` : `${import.meta.env.VITE_BACKEND_API_URL}canals/create`;
            let callObj = {
                canalTitle: name,
                canalDescription: description,
                validUntilDate: validityDate,
            }

            if (isEditPage) {
                let canal = searchParams.get("canal");
                callObj.canal = canal;
            }

            let response = await fetchWrapper.post(backendUrl, callObj);
            if (response.ok) {
                // Réinitialise les champs du formulaire après succès
                setName('');
                setDescription('');
                setValidityDate('');
                setLoading(false);
                showSwal("success",
                    isEditPage ? "Chat Edited Successfully" : "Chat Created Successfully"
                    , "Redirecting ..", (_) => navigate("/"));
                return 0;
            }
            setError(response.message)
            setLoading(false);
            return 0;
        } catch (error) {
            setError(error);
            setLoading(false);
        }
    };

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8">
            {
                bigLoader ?
                    <div role="status" className={"flex items-center justify-center"}>
                        <svg aria-hidden="true"
                             className="w-8 h-8 text-gray-200 animate-spin dark:text-gray-600 fill-blue-600"
                             viewBox="0 0 100 101" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path
                                d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z"
                                fill="currentColor"/>
                            <path
                                d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z"
                                fill="currentFill"/>
                        </svg>
                        <span className="sr-only">Loading...</span>
                    </div>
                    :
                    <>
                        <h2 className="text-2xl font-semibold text-gray-900">{isEditPage ? "Edit chat" : "Create New Chat!"}</h2>
                        <form onSubmit={handleSubmit} className="mt-6 space-y-6">
                            <div className="grid grid-cols-1 gap-y-6 sm:grid-cols-2 sm:gap-x-8">
                                <div>
                                    <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                                        Chat Name
                                    </label>
                                    <div className="mt-1">
                                        <input
                                            type="text"
                                            name="name"
                                            id="name"
                                            value={name}
                                            onChange={(e) => setName(e.target.value)}
                                            required
                                            className="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                        />
                                    </div>
                                </div>
                                <div>
                                    <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                                        Description
                                    </label>
                                    <div className="mt-1">
                                        <input
                                            type="text"
                                            name="description"
                                            id="description"
                                            value={description}
                                            onChange={(e) => setDescription(e.target.value)}
                                            required
                                            className="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                        />
                                    </div>
                                </div>
                                <div>
                                    <label htmlFor="validityDate" className="block text-sm font-medium text-gray-700">
                                        Validity Date
                                    </label>
                                    <div className="mt-1">
                                        <input
                                            type="date"
                                            name="validityDate"
                                            id="validityDate"
                                            value={validityDate}
                                            onChange={(e) => setValidityDate(e.target.value)}
                                            required
                                            className="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                        />
                                    </div>
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
                            <div className="flex justify-end">
                                <button
                                    type="submit"
                                    className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                                >
                                    {isEditPage ?
                                        loading ? "Editing Chat.." : "Edit Chat"
                                        :
                                        loading ? "Creating Chat.." : "Create Chat"
                                    }

                                </button>
                            </div>
                        </form>
                        {
                            isEditPage &&
                            <span>
                                <div className="max-w-screen mx-auto px-4 sm:px-6 lg:px-8 mt-8">
                                    <h2 className="text-2xl font-semibold text-gray-900">Utilisateurs ayant rejoint le chat : </h2>
                                    <div className="mt-6 overflow-x-auto">
                                        <table className="min-w-full bg-white divide-y divide-gray-200">
                                            <thead>
                                            <tr>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">First Name</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Last Name</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status (Bannis = Rouge)</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                                            </tr>
                                            </thead>
                                            <tbody className="bg-white divide-y divide-gray-200">
                                            {joinedUsers.map((user) => (
                                                <tr key={user.uid}>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{user.first_name}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{user.last_name}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{user.email}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                                        <div
                                                            className={`h-4 w-4 rounded-full ${user.is_user_enabled ? 'bg-green-500' : 'bg-red-500'}`}
                                                        />
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium flex space-x-4">
                                                        {
                                                            (user.is_user_enabled && user.uid !== chatToEdit.creator.uid) ?
                                                                <button
                                                                    onClick={() => handleKick(user.uid)}
                                                                    className="text-red-600 hover:text-red-900 inline-flex items-center space-x-2"
                                                                >
                                                                    <ArrowRightIcon className="h-5 w-5"/>
                                                                    <span>Kick du channel</span>
                                                                </button> : <button
                                                                    onClick={
                                                                        () => {
                                                                            showSwal("error", "Action Impossible", "Aucune action n'est possible sur le créateur du channel!")
                                                                        }
                                                                    }
                                                                    className="text-red-600 hover:text-red-900 inline-flex items-center space-x-2"
                                                                >
                                                                    <NoSymbolIcon className="h-5 w-5"/>
                                                                </button>
                                                        }
                                                    </td>
                                                </tr>
                                            ))}
                                            {joinedUsers.length === 0 && (
                                                <tr>
                                                    <td colSpan="5"
                                                        className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">No
                                                        Users in this canal.
                                                    </td>
                                                </tr>
                                            )}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </span>
                        }
                    </>
            }
        </div>
    );
};

export default ManageChat;
