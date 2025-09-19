

axios.defaults.withCredentials = true;  // 서버통신 오류(CORS) 방지
axios.defaults.timeout = 10000;  // 대기시간 1분

// 요청 가로채기
axios.interceptors.request.use(config => { 
    console.log(`[Request] ${config.method?.toUpperCase()}, ${config.url}`)

    return config;
}, error => {
    console.error('[Request Error]', error);
    return Promise.reject(error);  // 인터셉터에서 처리 한 후 promise 로 리턴해야 axios 호출 위치에서 처리 가능
});


// 응답 가로채기
axios.interceptors.response.use(response => { 
    return response;
}, error => {
    errorHandler(error);
    return Promise.reject(error);  // 인터셉터에서 처리 한 후 promise 로 리턴해야 axios 호출 위치에서 처리 가능
});

function errorHandler(error) {
    if (!error.response) {
        alert('네트워크 상태를 확인하십시오.');
        return false;
    }

    const status = error.response.status;
    const data = error.response.data;

    switch(status) {
        case 400:
            alert('잘못된 요청입니다.');
            break;
        case 401:
            alert('로그인이 필요한 기능입니다.');
            break;
        case 403:
            alert('귄한이 없습니다.');
            break;
        case 500:
            if (data.message !== null || data.message !== undefined || data.message.length > 0) {
                alert(data.message);
            } else {
                alert('서버 내부에 오류가 발생했습니다.');
            }
            
            break;
        default:
            alert('예상치 못한 오류가 발생했습니다.');
    }
}