import React, {useEffect, useState} from 'react';
import {fetchWrapper} from "../utils/fetch-wrapper.js";
import withReactContent from "sweetalert2-react-content";
import Swal from "sweetalert2";
import {NoSymbolIcon} from "@heroicons/react/24/solid/index.js";
import {CheckCircleIcon, ClockIcon, PlusCircleIcon, ShieldExclamationIcon} from "@heroicons/react/24/outline/index.js";
import {useNavigate} from "react-router-dom";

const InvitationManageComponent = () => {
    const [invitations, setInvitations] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [currentPage, setCurrentPage] = useState(1);
    const [itemsPerPage, setItemsPerPage] = useState(10);
    const [bigLoader, setBigLoader] = useState(true);

    const navigate = useNavigate();

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

    // Fonction pour afficher une popup de confirmation
    const showSwalConfirm = (invitation) => {
        withReactContent(Swal).fire({
            showConfirmButton: true,
            confirmButtonText: "Accepter",
            denyButtonText: "Refuser",
            denyButtonColor: "red",
            showCancelButton: false,
            showDenyButton: true,
            title: "Répondre à l'invitation",
            showLoaderOnConfirm: true,
            text: `Voulez-vous accepter ou refuser l'invitation à rejoindre ${invitation.canal.canal_title} ?`,
            allowOutsideClick: false,
            preDeny: async () => {
                return new Promise((resolve, reject) => {
                    fetchWrapper.post(`${import.meta.env.VITE_BACKEND_API_URL}invitations/answer`, {
                        invitationId: invitation.id,
                        canalId: invitation.canal.canal_id,
                        status: "DECLINED"
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
            },
            // Suite appelée lorsqu'on accepte l'invitation
            preConfirm: async () => {
                return new Promise((resolve, reject) => {
                    fetchWrapper.post(`${import.meta.env.VITE_BACKEND_API_URL}invitations/answer`, {
                        invitationId: invitation.id,
                        canalId: invitation.canal.canal_id,
                        status: "ACCEPTED"
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
                // Recharge les invitations après avoir répondu
                loadInvitations();
                showSwal("success", "Réponse envoyée", "Votre réponse a bien été envoyé!", (_) => {
                });
            }
        }).catch((error) => {
            if (error === "Invitation not found OR information mismatch OR invalid status") {
                loadInvitations();
                showSwal("error", "Erreur lors de la réponse à l'invitation", "Vous avez déjà répondu à cette invitation!", (_) => {
                });
                return;
            }
            showSwal("error", "Erreur lors de la réponse à l'invitation", "Une erreur est survenue lors de la réponse", (_) => {
            });
        })
    }

    // Fonction pour charger les invitations depuis l'API
    const loadInvitations = async () => {
        setBigLoader(true)
        try {
            const response = await fetchWrapper.get(`${import.meta.env.VITE_BACKEND_API_URL}invitations/getMyInvitations`);
            if (response.ok) {
                setInvitations(response.invitations);
                setBigLoader(false);
                console.log(response.invitations)
            } else {
                if (response.message === "Users have not been invited to any canal yet") {
                    setInvitations([]);
                    setBigLoader(false);
                    return;
                }
                console.error('Error while loading invitations');
            }
        } catch (error) {
            console.error(error)
            console.error('Error while loading invitations', error);
        }
    };

    // Charger les invitations au montage du composant
    useEffect(() => {
        loadInvitations();
    }, []);

    // Filtre les invitations en fonction de l'entrée dans la barre de recherche
    const filteredInvitations = invitations.filter(
        invitation =>
            invitation.canal.canal_title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            invitation.canal.canal_description.toLowerCase().includes(searchTerm.toLowerCase()) ||
            invitation.inviter.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            invitation.inviter.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            invitation.canal.creator.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            invitation.canal.creator.lastName.toLowerCase().includes(searchTerm.toLowerCase())
    );

    //Gestion de la pagination
    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentItems = filteredInvitations.slice(indexOfFirstItem, indexOfLastItem);

    function getInvitationStatus(invitation) {
        if (invitation.status === "PENDING") {
            return (
                <button
                    onClick={
                        () => {
                            showSwalConfirm(invitation);
                        }
                    }
                    className="text-green-600 hover:text-green-900 inline-flex items-center space-x-2"
                >
                    <CheckCircleIcon className="h-5 w-5"/>
                </button>
            )
        }
        if (invitation.status === "DECLINED") {
            return (
                <button
                    onClick={
                        () => {
                            showSwal("error", "Action Impossible", "Vous avez déjà quitté ce canal!", (_) => {
                            });
                        }
                    }
                    className="text-red-600 hover:text-red-900 inline-flex items-center space-x-2"
                >
                    <ShieldExclamationIcon className="h-5 w-5"/>
                </button>
            )
        }
        return (
            <button
                onClick={
                    () => {
                        showSwal("error", "Action Impossible", "Vous ne pouvez pas répondre une deuxième fois à une invitation, votre réponse était : " + invitation.status, (_) => {
                        });
                    }
                }
                className="text-red-600 hover:text-red-900 inline-flex items-center space-x-2"
            >
                <NoSymbolIcon className="h-5 w-5"/>
            </button>
        )
    }

    return (
        <div className="max-w-[85vw] mx-auto px-4 sm:px-6 lg:px-8 mt-8">
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
                        <h2 className="text-2xl font-semibold text-gray-900">Gérer mes invitations</h2>
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
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nom du Canal</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description du Canal</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Créateur du Canal </th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nom de l'inviteur </th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status du Canal </th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date de Création </th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date de fin de Validité </th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                                            </tr>
                                            </thead>
                                            <tbody className="bg-white divide-y divide-gray-200">
                                            {currentItems.map((invitation) => (
                                                <tr key={invitation.id}>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{invitation.canal.canal_title}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{invitation.canal.canal_description}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{`${invitation.canal.creator.firstName} ${invitation.canal.creator.lastName}`}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{`${invitation.inviter.firstName} ${invitation.inviter.lastName}`}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{new Date(invitation.canal.creation_date).toISOString().split('T')[0]}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{new Date(invitation.canal.valid_until).toISOString().split('T')[0]}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                                        <div
                                                            className={`h-4 w-4 rounded-full ${invitation.canal.is_canal_enabled ? 'bg-green-500' : 'bg-red-500'}`}
                                                        />
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium flex space-x-4">
                                                        {
                                                            getInvitationStatus(invitation)
                                                        }
                                                    </td>
                                                </tr>
                                            ))}
                                            {filteredInvitations.length === 0 && (
                                                <tr>
                                                    <td colSpan="5"
                                                        className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">
                                                        No invitations found.
                                                    </td>
                                                </tr>
                                            )}
                                            </tbody>
                                        </table>
                                    </div>
                                        <span className="ml-4 text-sm text-gray-700">
                                            Page {currentPage} sur {Math.ceil(filteredInvitations.length / itemsPerPage)} - {itemsPerPage} éléments par page
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
                                                className={`inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 ${currentPage === Math.ceil(filteredInvitations.length / itemsPerPage) ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                onClick={() => setCurrentPage(currentPage + 1)}
                                                disabled={currentPage === Math.ceil(filteredInvitations.length / itemsPerPage)}>
                                                Suivant
                                            </button>
                                        </span>
                                    </span>
                                </div>
                            </span>
                    </>
            }
        </div>
    )
};

export default InvitationManageComponent;
