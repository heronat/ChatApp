import React, {useEffect, useState} from 'react';
import {useLocation, useNavigate, useSearchParams} from "react-router-dom";
import {fetchWrapper} from "../utils/fetch-wrapper.js";

import Swal from 'sweetalert2'
import withReactContent from 'sweetalert2-react-content'
import {ArrowRightIcon, ClockIcon, PlusCircleIcon, ShieldExclamationIcon} from "@heroicons/react/24/outline/index.js";
import {NoSymbolIcon} from "@heroicons/react/24/solid/index.js";
import {useSelector} from "react-redux";

const InviteInChatComponent = () => {
    const [chatToEdit, setChatToEdit] = useState(undefined);

    const [bigLoader, setBigLoader] = useState(true);
    const [error, setError] = useState(undefined);

    const navigate = useNavigate();
    const location = useLocation();

    const [users, setUsers] = useState([]);

    const [searchParams, setSearchParams] = useSearchParams();

    const authUser = useSelector(x => x.auth.user);

    const [currentPage, setCurrentPage] = useState(1);
    const [itemsPerPage, setItemsPerPage] = useState(5);
    const [searchTerm, setSearchTerm] = useState("");

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

    // Filtre les utilisateurs en fonction du terme de recherche
    const filteredUsers = users.filter(
        user =>
            user.first_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            user.last_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    //Gestion de la pagination
    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentItems = filteredUsers.slice(indexOfFirstItem, indexOfLastItem);


    // Charge les utilisateurs depuis l'API
    const loadUsers = async (tempUsers) => {
        try {
            let response = await fetchWrapper.get(`${import.meta.env.VITE_BACKEND_API_URL}users/getUsers`);
            if (response.ok) {
                let newUsers = tempUsers;
                for (let i = 0; i < response.users.length; i++) {
                    let nvUser = response.users[i];
                    if (!newUsers.some((u) => parseInt(u.uid) === parseInt(nvUser.uid))) {
                        newUsers.push({
                            uid: nvUser.uid,
                            first_name: nvUser.firstName,
                            last_name: nvUser.lastName,
                            email: nvUser.email,
                            is_user_enabled: nvUser.userEnabled,
                            user_is_on_canal: false,
                            user_invited: false
                        })
                    }
                }
                return newUsers;
            }
            showSwal("error", "Error while loading users", "Error While loading users", (_) => navigate("/"));
            setBigLoader(false);
        } catch (error) {
            showSwal("error", "Error while loading users", "Error While loading users", (_) => navigate("/"));
            setBigLoader(false);
            throw error;
        }
    }

    // Charge les informations du chat depuis l'API
    const loadChatData = async () => {
        try {
            let canal = searchParams.get("canal");
            if (!!!canal) {
                throw new Error("Invalid Chat ID");
            }
            const resp = await fetchWrapper.get(`${import.meta.env.VITE_BACKEND_API_URL}canals/getCanalInfo?canal=${canal}`)
            if (resp.ok) {
                setChatToEdit(resp.canal);
                if (resp.canal.creator.uid !== authUser.id) {
                    showSwal("error", "Action Impossible", "Vous n'êtes pas autorisé à inviter des utilisateurs sur ce canal", (_) => navigate("/"));
                    throw new Error("Action Impossible");
                }
                return resp.canal.joined_users.map((user) => {
                    return {
                        uid: user.uid,
                        first_name: user.firstName,
                        last_name: user.lastName,
                        email: user.email,
                        is_user_enabled: user.userEnabled,
                        user_is_on_canal: true,
                        user_invited: true
                    }
                })
            }
        } catch (error) {
            if (error.message === "Action Impossible") {
                throw error;
            }
            console.error(error);
            showSwal("error", "Invalid Chat ID", "Please provide a valid chat ID", (_) => navigate("/"));
            throw error;
        }
    }

    // Charge la liste des utilisateurs invités depuis l'API
    const loadInvitedUsers = async (tempUsers) => {
        try {
            let canal = searchParams.get("canal");
            let response = await fetchWrapper.get(`${import.meta.env.VITE_BACKEND_API_URL}invitations/getcanalinvitations?canalId=${canal}`);
            if (response.ok) {
                let newUsers = tempUsers;
                for (let i = 0; i < response.invitations.length; i++) {
                    let nvUser = response.invitations[i].invitee;
                    let userIndex = newUsers.findIndex((u) => parseInt(u.uid) === parseInt(nvUser.uid));
                    if (userIndex !== -1) {
                        newUsers[userIndex].user_invited = true;
                        newUsers[userIndex].invitations_status = response.invitations[i].status;
                    }
                }
                setUsers(newUsers)
                setBigLoader(false);
                return 0;
            }
            if (response.message === "No invitations for this Canal") {
                setUsers(tempUsers)
                setBigLoader(false);
                return 0;
            }
            showSwal("error", "Error while loading users", "Error While loading users (#2)", (_) => navigate("/"));
            setBigLoader(false);
            return 0;
        } catch (error) {
            console.log(error)
            showSwal("error", "Error while loading users", "Error While loading users (#2)", (_) => navigate("/"));
            setBigLoader(false);
        }
    }

    const loadData = async () => {
        try {
            setBigLoader(true)
            let tempUsers = await loadChatData();
            let tempUser2 = await loadUsers(tempUsers);
            await loadInvitedUsers(tempUser2);
            return 0;
        } catch (e) {
            console.log(e)
        }
    }

    // Charger les données au montage du composant
    useEffect(
        () => {
            let canal = searchParams.get("canal");
            if (!!!canal) {
                showSwal("error", "Invalid Chat ID", "Please provide a valid chat ID", (_) => navigate("/"));
                return;
            }
            loadData();
        }, []
    )

    //Affichage d'une popup pour confirmer l'envoi de l'invitation
    const showSwalInvite = (user) => {
        withReactContent(Swal).fire({
            showConfirmButton: true,
            confirmButtonText: "Oui, Inviter",
            cancelButtonText: "Annuler",
            cancelButtonColor: "red",
            showCancelButton: true,
            title: "Inviter un utilisateur",
            showLoaderOnConfirm: true,
            text: `Voulez-vous vraiment inviter ${user.first_name} ${user.last_name} à ${chatToEdit.canal_title} ?`,
            allowOutsideClick: false,
            preConfirm: async (inputValue) => {
                return new Promise((resolve, reject) => {
                    fetchWrapper.post(`${import.meta.env.VITE_BACKEND_API_URL}invitations/invite`, {
                        canalId: chatToEdit.canal_id,
                        userId: user.uid
                    }).then((response) => {
                        if (response.ok) {
                            resolve(response);
                        } else {
                            reject(response);
                        }
                    }).catch((error) => {
                        reject(error);
                    })
                });
            }
        }).then((result) => {
            if (result.isConfirmed) {
                loadData();
                showSwal("success", "Utilisateur invité", "L'utilisateur a été invité avec succès", (_) => {});
            }
        }).catch((error) => {
            if (error === "User have already been invited to this canal") {
                showSwal("error", "Erreur lors de la création de l'invitation", "L'utilisateur a déjà été invité à ce canal", (_) => {});
                return;
            }
            showSwal("error", "Erreur", "Une erreur est survenue lors de l'invitation de l'utilisateur", (_) => {});
        })
    }

    function getInvitationComponent(invitation) {
        if (invitation.invitations_status === "PENDING") {
            return (
                <button
                    onClick={
                        () => {
                            showSwal("info", "Invitation déjà envoyé!", "Vous ne pouvez pas re-inviter un utilisateur déjà invité.")
                        }
                    }
                    className="text-yellow-600 hover:text-yellow-900 inline-flex items-center space-x-2"
                >
                    <ClockIcon className="h-5 w-5"/>
                </button>
            )
        }
        return (
            <button
                onClick={
                    () => {
                        showSwal("error", "Vous ne pouvez pas ré-inviter un utilisateur!", "Vous ne pouvez pas re-inviter un utilisateur ayant déjà refusé votre invitation!")
                    }
                }
                className="text-red-600 hover:text-red-900 inline-flex items-center space-x-2"
            >
                <ShieldExclamationIcon
                    className="h-5 w-5"/>
            </button>
        )
    }

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
                        <h2 className="text-2xl font-semibold text-gray-900">{`Inviter un utilisateur sur ${chatToEdit.canal_title}`}</h2>
                        <span>
                                <div className="max-w-screen mx-auto px-4 sm:px-6 lg:px-8 mt-8">
                                    <span
                                        className={"w-12"}
                                    >
                                            <input
                                                type="text"
                                                placeholder="Rechercher..."
                                                className={"bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"}
                                                value={searchTerm}
                                                onChange={e => setSearchTerm(e.target.value)}
                                            />
                                        </span>
                                    <div className="mt-6 overflow-x-auto">
                                        <table className="min-w-full bg-white divide-y divide-gray-200 pt-10">
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
                                            {currentItems.map((user) => (
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
                                                            (user.user_is_on_canal && user.user_is_on_canal ?
                                                                    <button
                                                                        onClick={
                                                                            () => {
                                                                                showSwal("error", "Action Impossible", "Vous ne pouvez pas inviter un utilisateur déjà sur le canal")
                                                                            }
                                                                        }
                                                                        className="text-red-600 hover:text-red-900 inline-flex items-center space-x-2"
                                                                    >
                                                                        <NoSymbolIcon className="h-5 w-5"/>
                                                                    </button> :
                                                                    user.user_invited ?
                                                                        getInvitationComponent(user)
                                                                        :
                                                                        <button
                                                                            onClick={
                                                                                () => {
                                                                                    showSwalInvite(user)
                                                                                }
                                                                            }
                                                                            className="text-green-600 hover:text-green-600-900 inline-flex items-center space-x-2"
                                                                        >
                                                                            <PlusCircleIcon className="h-5 w-5"/>
                                                                        </button>

                                                            )
                                                        }
                                                    </td>
                                                </tr>
                                            ))}
                                            {currentItems.length === 0 && (
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
                                        <span className="ml-4 text-sm text-gray-700">
                                            Page {currentPage} sur {Math.ceil(filteredUsers.length / itemsPerPage)} - {itemsPerPage} éléments par page
                                        </span>
                                    <span className={"flex justify-between pt-10"}>
                                        <button
                                            onClick={
                                                (_) => navigate("/")
                                            }
                                            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-400 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                                        >
                                                Retourner à la page principale.
                                        </button>
                                        <span className={"flex justify-between w-48"}>
                                            <button
                                                className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-400 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 ${currentPage === 1 ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                onClick={() => setCurrentPage(currentPage - 1)}
                                                disabled={currentPage === 1}>
                                                Précédent
                                            </button>
                                            <button
                                                className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 ${currentPage === Math.ceil(filteredUsers.length / itemsPerPage) ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                onClick={() => setCurrentPage(currentPage + 1)}
                                                disabled={currentPage === Math.ceil(filteredUsers.length / itemsPerPage)}>
                                                Suivant
                                            </button>
                                        </span>
                                    </span>
                                </div>
                            </span>
                    </>
            }
        </div>
    );
};

export default InviteInChatComponent;
