import React from 'react';
import {Navigate, Outlet} from 'react-router-dom';
import {useSelector} from "react-redux";


const PrivateRoutes = () => {
    //const isLoggedIn = Boolean(currentUser && currentUser.accessToken !== undefined);
    const { user: authUser } = useSelector(x => x.auth);
    return(
        authUser ?
            <Outlet />
            :
            <Navigate to="/login"/>
    )
}

export default PrivateRoutes
