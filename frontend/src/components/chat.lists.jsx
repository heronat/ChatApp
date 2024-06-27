import React, { useState, useEffect } from 'react';
import { PencilIcon, TrashIcon, ArrowRightIcon } from '@heroicons/react/24/outline';
import {fetchWrapper} from "../utils/fetch-wrapper.js";
import {store} from "../store/index.js";
import {useSelector} from "react-redux";
import {useNavigate} from "react-router-dom";
import {UserPlusIcon} from "@heroicons/react/24/outline/index.js";

import Swal from 'sweetalert2'
import withReactContent from 'sweetalert2-react-content'

const ChatsList = () => {
    const [joinedChats, setJoinedChats] = useState([]);
    const [createdChats, setCreatedChats] = useState([]);

    const navigate  = useNavigate();

    const [searchTermJoinedChats, setSearchTermJoinedChats] = useState("");
    const [currentPageJoinedChats, setCurrentPageJoinedChats] = useState(1);
    const [itemsPerPageJoinedChats, setItemsPerPageJoinedChats] = useState(5);

    const filteredJoinedChats = joinedChats.filter(
        chat =>
            chat.description.toLowerCase().includes(searchTermJoinedChats.toLowerCase()) ||
            chat.name.toLowerCase().includes(searchTermJoinedChats.toLowerCase())
    );

    const indexOfLastItemJoinedChats = currentPageJoinedChats * itemsPerPageJoinedChats;
    const indexOfFirstItemJoinedChats = indexOfLastItemJoinedChats - itemsPerPageJoinedChats;
    const currentItemsJoinedChats = filteredJoinedChats.slice(indexOfFirstItemJoinedChats, indexOfLastItemJoinedChats);

    const [searchTermCreatedChats, setSearchTermCreatedChats] = useState("");
    const [currentPageCreatedChats, setCurrentPageCreatedChats] = useState(1);
    const [itemsPerPageCreatedChats, setItemsPerPageCreatedChats] = useState(5);

    const filteredCreatedChats = createdChats.filter(
        chat =>
            chat.description.toLowerCase().includes(searchTermCreatedChats.toLowerCase()) ||
            chat.name.toLowerCase().includes(searchTermCreatedChats.toLowerCase())
    );

    const indexOfLastItemCreatedChats = currentPageCreatedChats * itemsPerPageCreatedChats;
    const indexOfFirstItemCreatedChats = indexOfLastItemCreatedChats - itemsPerPageCreatedChats;
    const currentItemsCreatedChats = filteredCreatedChats.slice(indexOfFirstItemCreatedChats, indexOfLastItemCreatedChats);

    const authUser = useSelector(x => x.auth.user);
    const handleJoin = (id) => {
        navigate(`/chat?canal=${id}`);
    };

    const handleEdit = (id) => {
        navigate(`/edit-chat?canal=${id}`);
    };

    const handleInvite = (id) => {
        navigate(`/invite?canal=${id}`);
    };

    //Supprimer un chat avec un id spécifique
    const handleDelete = (idString) => {
        console.log(`Delete chat with id: ${idString}`);
        const finalId = parseInt(idString, 10);
        if (!Number.isInteger(finalId) || finalId <= 0) {
            showSwal('error', 'Erreur', 'Impossible de quitter le chat, Veuillez réssayer plus tard!', () => {});
        }
        //Ouvrir une popup pour confirmer la suppression
        Swal.fire({
            title: "Êtes-vous sûr de vouloir supprimer ce chat?",
            text: "Si vous supprimer ce chat, vous ne pourrez pas revenir en arrière!",
            icon: "warning",
            showCancelButton: true,
            showLoaderOnConfirm: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Oui, supprimer!",
            cancelButtonText: "Annuler",
            preConfirm: async () => {
                return new Promise((resolve, reject) => {
                    fetchWrapper.delete(`${import.meta.env.VITE_BACKEND_API_URL}canals/delete/${finalId.toString()}`, {})
                        .then((response) => {
                            if (response.ok) {
                                resolve(response);
                            }
                            reject(response);
                        }).catch((error) => {
                            reject(error);
                        })
                });
            }
        }).then((result) => {
            if (result.isConfirmed) {
                // Mise à jour de l'état en filtrant le chat supprimé
                setCreatedChats( prevState => prevState.filter(chat => chat.id !== finalId));
                showSwal('success', 'Succès', 'Vous avez supprimé le chat avec succès!', () => {
                    location.reload();
                });
            }
        }).catch((error) => {
            console.error('Failed to delete chat: ', error);
            showSwal('error', 'Erreur', 'Impossible de supprimer le chat, Veuillez réssayer plus tard!', () => {});
        });
    };

    // Fonction pour quitter un chat avec un identifiant spécifique
    const handleLeave = (idString) => {
        console.log(`Leave chat with id: ${idString}`);
        const finalId = parseInt(idString, 10);
        if (!Number.isInteger(finalId) || finalId <= 0) {
            showSwal('error', 'Erreur', 'Impossible de quitter le chat, Veuillez réssayer plus tard!', () => {});
        }
        //Ouvrir une popup pour confirmer le départ
        Swal.fire({
            title: "Êtes-vous sûr de vouloir quitter ce chat?",
            text: "Si vous quitter ce chat, vous ne pourrez plus jamais le rejoindre!",
            icon: "warning",
            showCancelButton: true,
            showLoaderOnConfirm: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Oui, je veux quitter!",
            cancelButtonText: "Annuler",
            preConfirm: async () => {
                return new Promise((resolve, reject) => {
                    fetchWrapper.delete(`${import.meta.env.VITE_BACKEND_API_URL}canals/leave/${finalId.toString()}`, {})
                        .then((response) => {
                            if (response.ok) {
                                resolve(response);
                            }
                            reject(response);
                        }).catch((error) => {
                        reject(error);
                    })
                });
            }
        }).then((result) => {
            if (result.isConfirmed) {
                setJoinedChats( prevState => prevState.filter(chat => chat.id !== finalId));
                showSwal('success', 'Succès', 'Vous avez quitté le chat avec succès!', () => {
                    location.reload();
                });
            }
        }).catch((error) => {
            console.error('Failed to leave chat: ', error);
            showSwal('error', 'Erreur', 'Impossible de quitter le chat, Veuillez réssayer plus tard!', () => {});
        });
    };

    // URL de base pour les requêtes API liées aux canaux de chat
    const baseUrl = `${import.meta.env.VITE_BACKEND_API_URL}canals`;

    // Fonction pour afficher des notifications en haut à gauche de l'écran avec SweetAlert
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


    // Récupérer les canaux de chat créés et rejoints par l'utilisateur actuel à la création du composant
    useEffect(() => {
        const fetchData = async () => {
            try {
                const userChats = await fetchWrapper.get(`${baseUrl}/getMyCanals`);
                let createdChats = [];
                let joinedChats = [];
                if (userChats.canals) {
                    for (let chatNb in userChats.canals) {
                        let chat = userChats.canals[chatNb];
                        if (chat.creator.uid === authUser.id) {
                            createdChats.push({
                                id: chat.canal_id,
                                name: chat.canal_title,
                                description: chat.canal_description,
                                creationDate: new Date(chat.creation_date),
                                validUntilDate: new Date(chat.valid_until),
                                nbRegistred: chat.joined_users.length,
                                is_canal_enabled: chat.is_canal_enabled
                            })
                        } else {
                            joinedChats.push({
                                id: chat.canal_id,
                                name: chat.canal_title,
                                description: chat.canal_description,
                                creationDate: new Date(chat.creation_date),
                                validUntilDate: new Date(chat.valid_until),
                                nbRegistred: chat.joined_users.length,
                                is_canal_enabled: chat.is_canal_enabled
                            })
                        }
                    }
                }

                setCreatedChats(createdChats)
                setJoinedChats(joinedChats)

                console.log('Fetched data Created: ', createdChats)
                console.log('Fetched data Joined: ', joinedChats)
            } catch (error) {
                console.error('Failed to fetch data: ', error);
            }
        };

        fetchData();

        const intervalId = setInterval(fetchData, 15000);
        return () => clearInterval(intervalId);
    }, []);

    return (
        <>
            <div className="max-w-screen mx-auto px-4 sm:px-6 lg:px-8 mt-8">
                <h2 className="text-2xl font-semibold text-gray-900">Chats Crées</h2>
                <div className="mt-6 overflow-x-auto">
                    <input
                        type="text"
                        placeholder="Rechercher... (Nom ou Description)"
                        className={"mt-5 mb-5 bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"}
                        value={searchTermCreatedChats}
                        onChange={e => setSearchTermCreatedChats(e.target.value)}
                    />
                    <table className="min-w-full bg-white divide-y divide-gray-200">
                        <thead>
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Creation
                                Date
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date
                                de fin de Validité
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre
                                d'inscrits
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {currentItemsCreatedChats.map((chat) => (
                            <tr key={chat.id}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{chat.name}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{chat.description}</td>
                                <td className="px-6 py-4 whitespace-nowrap
                                text-sm text-gray-500">{new Date(chat.creationDate).toLocaleDateString()}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(chat.validUntilDate).toLocaleDateString()}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{chat.nbRegistred}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                    <div
                                        className={`h-4 w-4 rounded-full ${chat.is_canal_enabled ? 'bg-green-500' : 'bg-red-500'}`}
                                    />
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium flex space-x-4">
                                    <button
                                        onClick={() => handleJoin(chat.id)}
                                        className="text-indigo-600 hover:text-indigo-900 inline-flex items-center space-x-2"
                                    >
                                        <ArrowRightIcon className="h-5 w-5"/>
                                        <span>Join</span>
                                    </button>
                                    <button
                                        onClick={() => handleEdit(chat.id)}
                                        className="text-yellow-600 hover:text-yellow-900 inline-flex items-center space-x-2"
                                    >
                                        <PencilIcon className="h-5 w-5"/>
                                        <span>Edit</span>
                                    </button>
                                    <button
                                        onClick={() => handleInvite(chat.id)}
                                        className="text-green-600 hover:text-green-900 inline-flex items-center space-x-2"
                                    >
                                        <UserPlusIcon className="h-5 w-5"/>
                                        <span>Inviter</span>
                                    </button>
                                    <button
                                        onClick={() => handleDelete(chat.id)}
                                        className="text-red-600 hover:text-red-900 inline-flex items-center space-x-2"
                                    >
                                        <TrashIcon className="h-5 w-5"/>
                                        <span>Delete</span>
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {filteredCreatedChats.length === 0 && (
                            <tr>
                                <td colSpan="5"
                                    className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">No chats
                                    joined
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                    <div
                        className={"flex justify-between"}
                    >
                        <span className="ml-4 text-sm text-gray-700">
                                            Page {currentPageCreatedChats} sur {Math.ceil(filteredCreatedChats.length / itemsPerPageCreatedChats)} - {itemsPerPageCreatedChats} éléments par page
                        </span>
                        <span className={"flex justify-between w-48"}>
                                            <button
                                                className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-400 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 ${currentPageCreatedChats === 1 ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                onClick={() => setCurrentPageCreatedChats(currentPageCreatedChats - 1)}
                                                disabled={currentPageCreatedChats === 1}>
                                                Précédent
                                            </button>
                                            <button
                                                className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 ${currentPageCreatedChats === Math.ceil(filteredCreatedChats.length / itemsPerPageCreatedChats) ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                onClick={() => setCurrentPageCreatedChats(currentPageCreatedChats + 1)}
                                                disabled={currentPageCreatedChats === Math.ceil(filteredCreatedChats.length / itemsPerPageCreatedChats)}>
                                                Suivant
                                            </button>
                        </span>
                    </div>
                </div>
            </div>
            <div className="max-w-screen mx-auto px-4 sm:px-6 lg:px-8 mt-8">
                <h2 className="text-2xl font-semibold text-gray-900">Chats Rejoints</h2>
                <div className="mt-6 overflow-x-auto">
                    <input
                        type="text"
                        placeholder="Rechercher... (Nom ou Description)"
                        className={"mt-5 mb-5 bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"}
                        value={searchTermJoinedChats}
                        onChange={e => setSearchTermJoinedChats(e.target.value)}
                    />
                    <table className="min-w-full bg-white divide-y divide-gray-200">
                        <thead>
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Creation
                                Date
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date
                                de fin de Validité
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {currentItemsJoinedChats.map((chat) => (
                            <tr key={chat.id}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{chat.name}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{chat.description}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(chat.creationDate).toLocaleDateString()}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(chat.validUntilDate).toLocaleDateString()}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                    <div
                                        className={`h-4 w-4 rounded-full ${chat.is_canal_enabled ? 'bg-green-500' : 'bg-red-500'}`}
                                    />
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium flex space-x-4">
                                    <button
                                        onClick={() => handleJoin(chat.id)}
                                        className="text-indigo-600 hover:text-indigo-900 inline-flex items-center space-x-2"
                                    >
                                        <ArrowRightIcon className="h-5 w-5"/>
                                        <span>Join</span>
                                    </button>
                                    <button
                                        onClick={() => handleLeave(chat.id)}
                                        className="text-red-600 hover:text-red-900 inline-flex items-center space-x-2"
                                    >
                                        <TrashIcon className="h-5 w-5"/>
                                        <span>Leave</span>
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {filteredJoinedChats.length === 0 && (
                            <tr>
                                <td colSpan="5"
                                    className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">No chats
                                    joined
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                    <div
                        className={"flex justify-between"}
                    >
                        <span className="ml-4 text-sm text-gray-700">
                                            Page {currentPageJoinedChats} sur {Math.ceil(filteredJoinedChats.length / itemsPerPageJoinedChats)} - {itemsPerPageJoinedChats} éléments par page
                        </span>
                        <span className={"flex justify-between w-48"}>
                                            <button
                                                className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-400 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 ${currentPageJoinedChats === 1 ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                onClick={() => setCurrentPageJoinedChats(currentPageJoinedChats - 1)}
                                                disabled={currentPageJoinedChats === 1}>
                                                Précédent
                                            </button>
                                            <button
                                                className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 ${currentPageJoinedChats === Math.ceil(filteredJoinedChats.length / itemsPerPageJoinedChats) ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                onClick={() => setCurrentPageJoinedChats(currentPageJoinedChats + 1)}
                                                disabled={currentPageJoinedChats === Math.ceil(filteredJoinedChats.length / itemsPerPageJoinedChats)}>
                                                Suivant
                                            </button>
                        </span>
                    </div>
                </div>
            </div>
        </>
    );
};

export default ChatsList;
