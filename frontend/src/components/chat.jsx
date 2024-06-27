import React, {useEffect, useRef, useState} from 'react';
import {useLocation, useNavigate, useSearchParams} from "react-router-dom";
import {fetchWrapper} from "../utils/fetch-wrapper.js";

import Lottie from "lottie-react";

import chatLoadingAnnimation from "/public/lotties/chat-load.json";
import logoutHamsterAnnimation from "/public/lotties/logout-hamster.json";
import errorLottieAnnimation from "/public/lotties/error-lottie.json";
import {useSelector} from "react-redux";
import EmojiPicker from "emoji-picker-react";

import Swal from 'sweetalert2'
import withReactContent from 'sweetalert2-react-content'

const ChatComponent = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [chatInfo, setChat] = useState(undefined);
    const [bigLoader, setBigLoader] = useState(true);
    const [logoutHamster, setLogoutHamster] = useState(false);
    const [errorAnnimation, setErrorAnnimation] = useState(false);

    const [showBoard, setShowBoard] = useState(false);

    const [connectedUsers, setConnectedUsers] = useState([]);

    const [ws, setWs] = useState(null);

    const [msgs, setMsgs] = useState([]);

    const [chatValue, setChatValue] = useState('');

    const [isTyping, setIsTyping] = useState(false);
    const [typersList, setTypersList] = useState([]);
    const [typingString, setTypingString] = useState("");

    const navigate = useNavigate();
    const location = useLocation();

    const inputFile = useRef(null)

    const authUser = useSelector(x => x.auth.user);

    // Afficher une notification de succès en haut à droite de l'écran
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

    // Afficher une notification d'erreur si l'utilisateur envoie un message inapproprié
    const showSwalInappropriate = (text, subtitle, onClose) => {
        withReactContent(Swal).fire({
            icon: "error",
            title: text,
            showConfirmButton: true,
            confirmButtonText: "Promis, je ne le referai plus !",
            text: subtitle,
            allowOutsideClick: false,
            didOpen: (toast) => {
                Swal.showLoading();
                setTimeout(() => {
                    Swal.hideLoading();
                }, 5000)
            },
            didClose: onClose
        })
    }

    const baseUrl = `${import.meta.env.VITE_BACKEND_API_URL}canals`;

    // Fonction pour ouvrir un chat avec un identifiant spécifique
    const openChat = async (chatId) => {
        try {
            let response = await fetchWrapper.post(`${baseUrl}/open`, {
                canalId: chatId
            });
            if (!response.ok) {
                throw new Error("An error occurred while opening chat. Please try again later.");
            }
            return response.socket_url;
        } catch (error) {
            throw error;
        }
    }

    // Fonction pour charger les informations d'un chat avec un identifiant spécifique
    const loadChat = async (chatId) => {
        try {
            let response = await fetchWrapper.get(`${baseUrl}/getCanalInfo?canal=${chatId}`);
            if (response.ok) {
                // Mise à jour de l'état avec les informations du chat
                setChat(response.canal);
                console.log("Canal : ", response.canal)

                // Mise à jour de l'état avec la liste des utilisateurs connectés au chat
                setConnectedUsers(response.canal.connected_users.map(user => {
                    return {
                        id: user.uid, first_name: user.firstName, last_name: user.userLastName, email: user.email,
                    }
                }))
            }
        } catch (error) {
            throw error;
        }
    }

    // Fonction pour se connecter à un chat en utilisant une websocket
    const connect = async () => {
        let canal = searchParams.get("canal");
        try {
            await loadChat(canal);
            let socketUrl = await openChat(canal);

            // Construction de l'URL complète du WebSocket avec le token d'authentification
            let fullSocketUrl = `${import.meta.env.VITE_WS_URL}${socketUrl}?token=${authUser.accessToken}`;

            const websocket = new WebSocket(fullSocketUrl);

            // Gestion de l'ouverture de la connexion WebSocket
            websocket.onopen = () => {
                console.log('Connected to WebSocket');
                setMsgs(prevState => [...prevState, {
                    message: "Connecté au chat avec succès. Vous pouvez maintenant commencer à discuter.",
                    senderId: -1,
                    sender: "SYSTEM",
                    type: "MESSAGE",
                }])
            };

            // Gestion des messages reçus sur la websocket
            websocket.onmessage = (event) => {
                console.log('Received data from WebSocket:', event.data);
                let data = JSON.parse(event.data);

                if (data.sender === "SYSTEM") {
                    console.warn("System message received : ", data)
                }

                if (data.type === "ERROR") {
                    if (ws !== null && (ws.readyState === 1 || ws.readyState === 0)) {
                        ws.close()
                    }
                    setErrorAnnimation(true);
                    showSwal("error", "Error", data.message, (_) => navigate("/"));
                }

                if (data.type === "LEAVE" || data.type === "JOIN") {
                    if (data.type === "JOIN") {
                        console.log("User joined : ", data.senderId)
                        setConnectedUsers(prevState => [...prevState, {
                            id: data.senderId,
                            first_name: data.userFirstName,
                            last_name: data.userLastName,
                            email: data.userEmail,
                        }])
                        showSwal("info", "+ Membre", `${data.userFirstName} ${data.userLastName} a rejoint le chat.`)
                    }
                    if (data.type === "LEAVE") {
                        showSwal("info", "- Membre", `${data.userFirstName} ${data.userLastName} a quitté le chat.`)
                        setConnectedUsers(prevState => prevState.filter(user => user.id !== data.senderId))
                    }
                }
                console.log(data.type)
                console.log(data.type === "TYPING_START")

                if (data.type === "TYPING_START") {
                    if (!typersList.some(user => user.id === data.senderId)) {
                        setTypersList(prevState => [...prevState, {
                            id: data.senderId,
                            first_name: data.userFirstName,
                            last_name: data.userLastName,
                            email: data.userEmail,
                        }])
                    }
                    console.log(typersList)
                    return;
                }

                if (data.type === "TYPING_STOP") {
                    setTypersList(prevState => prevState.filter(user => user.id !== data.senderId))
                    return;
                }

                setMsgs(prevArray => [...prevArray, data]);
            };

            websocket.onerror = (error) => {
                console.log('WebSocket error:', error);
            };

            websocket.onclose = () => {
                console.log('WebSocket connection closed');
            };

            // Mettez à jour l'état avec l'instance WebSocket
            setWs(websocket);
            setBigLoader(false);
        } catch (error) {
            console.error(error)
            showSwal("error", "Error", "An error occurred. Please try again later.", (_) => navigate("/"));
        }
    }

    // Fermer le WebSocket lors du démontage du composant
    useEffect(() => {
        if (ws) {
            return () => {
                ws.close();
            };
        }
    }, []);

    // Initialiser la connexion au chat
    useEffect(() => {
        let canal = searchParams.get("canal");
        if (!!!canal) {
            console.log(canal)
            showSwal("error", "Invalid Chat ID", "Please provide a valid chat ID", (_) => navigate("/"));
            return;
        }
        setBigLoader(true);
        connect()
    }, [])

    // envoi d'une image en base64
    const handleFileChange = (event) => {
        const file = event.target.files[0];

        const reader = new FileReader();

        reader.onloadend = () => {
            const base64String = reader.result.replace("data:", "").replace(/^.+,/, "");
            console.log(base64String)
            ws.send(JSON.stringify({
                type: "PICTURE", message: base64String,
            }))
        }
        reader.readAsDataURL(file);
    };

    useEffect(() => {
        if (chatValue) {
            setIsTyping(true);
            const typingTimer = setTimeout(() => {
                setIsTyping(false);
                console.log("Stopped typing")
                ws.send(JSON.stringify({
                    type: "TYPING_STOP",
                    message: "Typing...",
                }))
            }, 5000);

            return () => clearTimeout(typingTimer);
        }
    }, [chatValue]);

    //Afficher un message sur le chat lorsqu'un utilisateur commence à taper
    function handleTyping(e) {
        if (!isTyping) {
            console.log("Started typing")
            ws.send(JSON.stringify({
                type: "TYPING_START",
                message: "Typing...",
            }))
        }
        setChatValue(e.target.value);
    }

    // Fonction pour gérer l'envoie d'un message dans la websocket
    const handleSubmit = async (e) => {
        e.preventDefault();
        const msg = chatValue;
        ws.send(JSON.stringify({
            type: "MESSAGE", message: msg,
        }))
        ws.send(JSON.stringify({
            type: "TYPING_STOP",
            message: "Typing...",
        }))
        console.log(msg);
        setIsTyping(false);
        setShowBoard(false)
        setChatValue("");
    };

    //Afficher une notification si un message inapproprié est envoyé
    useEffect(() => {
        let lastMsg = msgs[msgs.length - 1];
        if (lastMsg) {
            if (lastMsg.sender === "SYSTEM" && lastMsg.type === "INAPPROPRIATE_MESSAGE") {
                showSwalInappropriate("Message inapproprié !", lastMsg.message)
            }
        }
    }, [msgs])

    const parseImageBase64 = (base64) => {
        return `data:image/png;base64,${base64}`
    }

    // Obtenir une chaîne de caractères en fonction de si un ou plusieurs utilisateurs sont en train d'écrire
    const getTypingString = () => {
        let typerString = "";
        let typerUncounted = 0;

        for (let i = 0; i < typersList.length; i++) {
            if (typerString.length < 20) {
                typerString += `${typersList[i].first_name} ${typersList[i].last_name}, `;
            } else {
                typerUncounted++;
            }
        }

        return typerString.length > 0 ? `> ${typerString} ${typerUncounted === 0 ? 'est' : `et ${typerUncounted} autres utilisateurs sont`} en train d'écrire...` : "";
    }

    useEffect(() => {
        setTypingString(getTypingString())
    }, [typersList])

    const parseMessage = (msg, index) => {
        if (msg.sender === "SYSTEM") {
            if (msg.type === "INAPPROPRIATE_MESSAGE") {
                return (<div key={index} className={"flex items-center justify-center"}>
                    <span className={"pt-3 italic text-xs text-red-400 dark:text-gray-500"}>{msg.message}</span>
                </div>)
            }
            // Notification
            return (<div key={index} className={"flex items-center justify-center"}>
                <span className={"pt-3 italic text-xs text-gray-400 dark:text-gray-500"}>{msg.message}</span>
            </div>)
        }
        if (msg.senderId === authUser.id) {
            if (msg.type === "PICTURE") {
                return (<span key={index} className={"flex justify-end"}>
                        <div className="m-3 my-5 flex items-start gap-2.5">
                            <div className="flex flex-col w-full max-w-[320px] leading-1.5 p-4 bg-blue-200 rounded-e-xl rounded-es-xl dark:bg-blue-700">
                                <div className="flex items-center space-x-2 rtl:space-x-reverse">
                                    <span className="text-sm font-semibold text-gray-900 dark:text-white">{msg.sender}</span>
                                    <span className="text-sm font-normal text-gray-500 dark:text-gray-400">11:46</span>
                                </div>
                                <img src={parseImageBase64(msg.message)} className="rounded-lg max-h-fit max-w-fit "/>
                                <span className="text-sm font-normal text-gray-500 dark:text-gray-400">Delivered</span>
                            </div>
                        </div>
            </span>)
            }
            return (<span key={index} className={"flex justify-end"}>
                        <div className="m-3 my-5 flex items-start gap-2.5">
                        <div
                            className="flex flex-col w-full max-w-[320px] leading-1.5 p-4 bg-blue-200 rounded-e-xl rounded-es-xl dark:bg-blue-700">
                            <div className="flex items-center space-x-2 rtl:space-x-reverse">
                                <span
                                    className="text-sm font-semibold text-gray-900 dark:text-white">{msg.sender}</span>
                                <span className="text-sm font-normal text-gray-500 dark:text-gray-400">11:46</span>
                            </div>
                            <p className="text-sm font-normal py-2.5 text-gray-900 dark:text-white">{msg.message}</p>
                            <span className="text-sm font-normal text-gray-500 dark:text-gray-400">Delivered</span>
                        </div>
                    </div>

                    </span>)
        }
        if (msg.type === "PICTURE") {
            return (<div className="m-3 my-5 flex items-start gap-2.5">
                <div
                    className="flex flex-col w-full max-w-[320px] leading-1.5 p-4 border-gray-200 bg-gray-100 rounded-e-xl rounded-es-xl dark:bg-gray-700">
                    <div className="flex items-center space-x-2 rtl:space-x-reverse">
                                <span
                                    className="text-sm font-semibold text-gray-900 dark:text-white">{msg.sender}</span>
                        <span className="text-sm font-normal text-gray-500 dark:text-gray-400">11:46</span>
                    </div>
                    <img src={parseImageBase64(msg.message)} className="rounded-lg"/>
                    <span className="text-sm font-normal text-gray-500 dark:text-gray-400">Delivered</span>
                </div>
            </div>)
        }
        return (<div key={index} className="m-3 my-5 flex items-start gap-2.5">
            <div
                className="flex flex-col w-full max-w-[320px] leading-1.5 p-4 border-gray-200 bg-gray-100 rounded-e-xl rounded-es-xl dark:bg-gray-700">
                <div className="flex items-center space-x-2 rtl:space-x-reverse">
                                <span
                                    className="text-sm font-semibold text-gray-900 dark:text-white">{msg.sender}</span>
                    <span className="text-sm font-normal text-gray-500 dark:text-gray-400">11:46</span>
                </div>
                <p className="text-sm font-normal py-2.5 text-gray-900 dark:text-white">{msg.message}</p>
                <span className="text-sm font-normal text-gray-500 dark:text-gray-400">Delivered</span>
            </div>
        </div>)
    }

    return (
        errorAnnimation ? (
                <div
                    className={"w-full min-h-[90vh] flex justify-center items-center"}
                >
                <span className={"h-[50vh]"}>
                    <Lottie className={"h-3/6"} animationData={errorLottieAnnimation} loop={true}/>
                    <h3 className={"mt-10 text-gray-500 dark:text-gray-400 text-center"}>ERROR</h3>
                </span>
                </div>
            )
            :

            bigLoader ? (<div
                    className={"w-full min-h-[90vh] flex justify-center items-center"}
                >
                <span className={"h-[50vh]"}>
                    <Lottie className={"h-3/6"} animationData={chatLoadingAnnimation} loop={true}/>
                    <h3 className={"mt-10 text-gray-500 dark:text-gray-400 text-center"}>Loading Chat...</h3>
                </span>
    </div>) :

        logoutHamster ? (<div
                className={"w-full min-h-[90vh] flex justify-center items-center"}
            >
                <span className={"h-[50vh]"}>
                    <Lottie className={"h-3/6"} animationData={logoutHamsterAnnimation} loop={true}/>
                    <h3 className={"mt-10 text-gray-500 dark:text-gray-400 text-center"}>Logging out from Chat...</h3>
                </span>
            </div>) :
            (<span
                onClick={() => {
                    if (showBoard) setShowBoard(false)
                }}
                className={"flex justify-center w-full min-h-[90vh] h-5/6 max-h-[90vh]"}
            >
            <div className="flex justify-center w-4/5 min-h-[85vh] h-5/6 max-h-[85vh]">
                    <div
                        className="w-4/5 mx-auto rounded-lg bg-gray-50 min-h-[85vh] h-5/6 max-h-[85vh] overflow-scroll relative flex flex-col justify-between items-stretch">
                <span
                    className={"flex flex-col"}
                >
                    {msgs.map((msg, index) => (parseMessage(msg, index)))}
                </span>
                        <span
                            className={"flex flex-col justify-end"}
                        >
                            <h1 className="ml-10 text-sm font-semibold text-gray-900 dark:text-white" style={{
                                fontFamily: 'Gill sans', fontSize: "18px", fontWeight: 200,
                            }}> {typingString}</h1>
                            <span>
                                                            <EmojiPicker
                                                                onEmojiClick={(event, emojiObject) => {
                                                                    console.log(event.emoji)
                                                                    setChatValue(chat => chat + event.emoji)
                                                                }}
                                                                open={showBoard}
                                                            />
                            <input type='file' id='file' accept={"image/*"} ref={inputFile} onChange={handleFileChange}
                                   style={{display: 'none'}}/>
                    <form
                        onSubmit={handleSubmit}
                    >
                        <label htmlFor="chat" className="sr-only">Your message</label>
                        <div className="flex items-center px-3 py-2 rounded-lg bg-gray-50 dark:bg-gray-700">
                                    <button type="button"
                                            onClick={() => inputFile.current.click()}
                                            className="inline-flex justify-center p-2 text-gray-500 rounded-lg cursor-pointer hover:text-gray-900 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-white dark:hover:bg-gray-600">
            <svg className="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                 viewBox="0 0 20 18">
                <path fill="currentColor"
                      d="M13 5.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0ZM7.565 7.423 4.5 14h11.518l-2.516-3.71L11 13 7.565 7.423Z"/>
                <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                      d="M18 1H2a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h16a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1Z"/>
                <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                      d="M13 5.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0ZM7.565 7.423 4.5 14h11.518l-2.516-3.71L11 13 7.565 7.423Z"/>
            </svg>
            <span className="sr-only">Upload image</span>
        </button>
        <button type="button"
                onClick={() => setShowBoard(!showBoard)}
                className="p-2 text-gray-500 rounded-lg cursor-pointer hover:text-gray-900 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-white dark:hover:bg-gray-600">
            <svg className="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                 viewBox="0 0 20 20">
                <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                      d="M13.408 7.5h.01m-6.876 0h.01M19 10a9 9 0 1 1-18 0 9 9 0 0 1 18 0ZM4.6 11a5.5 5.5 0 0 0 10.81 0H4.6Z"/>
            </svg>
            <span className="sr-only">Add emoji</span>
        </button>
                            <textarea id="chat" rows="1"
                                      value={chatValue}
                                      onChange={handleTyping}
                                      className="block mx-4 p-2.5 w-full text-sm text-gray-900 bg-white rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                                      placeholder="Your message...">
                            </textarea>
                            <button type="submit"
                                    className="inline-flex justify-center p-2 text-blue-600 rounded-full cursor-pointer hover:bg-blue-100 dark:text-blue-500 dark:hover:bg-gray-600">
                                <svg className="w-5 h-5 rotate-90 rtl:-rotate-90" aria-hidden="true"
                                     xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 18 20">
                                    <path
                                        d="m17.914 18.594-8-18a1 1 0 0 0-1.828 0l-8 18a1 1 0 0 0 1.157 1.376L8 18.281V9a1 1 0 0 1 2 0v9.281l6.758 1.689a1 1 0 0 0 1.156-1.376Z"/>
                                </svg>
                                <span className="sr-only">Send message</span>
                            </button>
                        </div>
                    </form>
                            </span>
                        </span>
                </div>
                        <div className="w-px bg-gray-300"/>
                    <div
                        className="w-1/5 mx-auto rounded-lg bg-gray-50 min-h-[85vh] h-5/6 max-h-[85vh] overflow-scroll relative flex flex-col ">
                        <h3
                            style={{
                                fontFamily: 'Pacifico', fontSize: "24px", fontWeight: 200, textAlign: 'center',
                            }}
                            className={"pb-4"}
                        >
                            {chatInfo?.canal_title}
                        </h3>
                        <div className="h-px bg-gray-300"/>
                        <h3
                            style={{
                                fontFamily: 'Gill sans', fontSize: "18px", fontWeight: 200,
                            }}
                            className={"p-4 flex flex-row"}
                        >
                            Status de la Connexion : <div className={`ml-4 mt-1.5 h-4 w-4 rounded-full bg-green-500`}/>
                        </h3>
                        <span
                            className={"mb-5 flex justify-center items-center"}
                        >
                            <button
                                onClick={() => {
                                    setLogoutHamster(true)
                                    ws.close();
                                    showSwal("info", "Déconnexion", "Vous avez été déconnecté du chat avec succès.", (_) => navigate("/"));
                                }}
                                className={` flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 }`}>
                                                Se décconecter
                            </button>
                        </span>
                        <div className="h-px bg-gray-300"/>
                        <h3
                            style={{
                                fontFamily: 'Gill sans', fontSize: "18px", fontWeight: 200,
                            }}
                            className={"p-4"}
                        >
                            Description : {chatInfo?.canal_description}
                        </h3>
                        <div className="h-px bg-gray-300"/>
                                                <h3
                                                    style={{
                                                        fontFamily: 'Gill sans', fontSize: "18px", fontWeight: 200,
                                                    }}
                                                    className={"p-4"}
                                                >
                            Créateur : {chatInfo.creator.firstName} {chatInfo.creator.lastName}
                        </h3>
                        <div className="h-px bg-gray-300"/>
                        <h3
                            style={{
                                fontFamily: 'Gill sans', fontSize: "18px", fontWeight: 200,
                            }}
                            className={"p-4"}
                        >
                            Nombre de participants Total : {chatInfo.joined_users.length}
                        </h3>
                        <div className="h-px bg-gray-300"/>
                        <h3
                            style={{
                                fontFamily: 'Gill sans', fontSize: "18px", fontWeight: 200,
                            }}
                            className={"p-4"}
                        >
                            Nombre de Connecté Actuellement : {connectedUsers.length}
                        </h3>
                        <div className="h-px bg-gray-300"/>
                        <h3
                            style={{
                                fontFamily: 'Gill sans', fontSize: "18px", fontWeight: 200,
                            }}
                            className={"p-4"}
                        >
                            Liste des membres connectés :
                        </h3>
                        {connectedUsers.map((user, index) => (<div
                            key={index}
                            className="m-4 max-w-sm bg-white border border-gray-200 rounded-lg shadow dark:bg-gray-800 dark:border-gray-700">
                            <div className="flex flex-col items-center pb-5">
                                <h5 className="mb-1 text-xl font-medium text-gray-900 dark:text-white"> {user.first_name} {user.last_name} </h5>
                                <span
                                    className="text-sm text-gray-500 dark:text-gray-400">{user.email}</span>
                            </div>
                        </div>))}
                    </div>
                </div>
                </span>));
};

export default ChatComponent;

