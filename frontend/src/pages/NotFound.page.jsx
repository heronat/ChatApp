import React from 'react';

class NotFound extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <>
                <div className="flex items-center justify-center min-h-screen">
                    <div className="text-center">
                        <h1 className="mb-4 text-9xl font-semibold text-red-500">404</h1>
                        <p className="mb-4 text-4xl text-gray-600">Oops! On dirait que vous êtes perdu :(</p>
                        <div className="animate-bounce">
                            <svg className="mx-auto h-24 w-24 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                                      d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
                            </svg>
                        </div>
                        <p className="mt-4 text-gray-600">Retournons à la <a href="/" className="text-blue-500">maison</a>.</p>
                    </div>
                </div>
            </>
    )

    }
}

export default NotFound;
