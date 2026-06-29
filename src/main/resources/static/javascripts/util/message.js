function Message(url, password) {
    
    this.url = url.trim();
    this.password = password;

    this._connect = function () {

        return new Promise((accept, reject) => {
            let parmURL = `/navigator/connect?url=${encodeURIComponent(this.url)}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);
            xhttp.setRequestHeader("password", this.password);

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {

                    if (this.readyState === 4 && this.status === 200) {
                        var response = JSON.parse(this.responseText);

                        accept({
                            status: this.status,
                            response: response
                        });

                    } else {
                        reject({
                            status: this.status,
                            response: this.responseText
                        });

                    }

                };

                xhttp.onerror = function () {

                    alert("Unable to Login");

                };

            }

            xhttp.send();

        });

    }.bind(this);

    this._status = function () {

        return new Promise((accept, reject) => {
            let parmURL = `/navigator/status?url=${encodeURIComponent(this.url)}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);
            xhttp.setRequestHeader("password", this.password);

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {

                    if (this.readyState === 4 && this.status === 200) {
                        var response = JSON.parse(this.responseText);

                        accept({
                            status: this.status,
                            response: response
                        });

                    } else {
                        reject({
                            status: this.status,
                            response: this.responseText
                        });

                    }

                };

                xhttp.onerror = function () {

                    alert("nable to retrieve status");

                };

            }

            xhttp.send();

        });

    }.bind(this)

    this._search = function (argument, limit) {

        return new Promise((accept, reject) => {
            let parmURL = `/navigator/search?url=${encodeURIComponent(this.url)}&argument=${encodeURIComponent(argument)}&limit=${limit}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);
            xhttp.setRequestHeader("password", this.password);

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {

                    if (this.readyState === 4 && this.status === 200) {
                        var response = JSON.parse(this.responseText);

                        accept({
                            status: this.status,
                            response: response
                        });

                    } else {
                        reject({
                            status: this.status,
                            response: this.responseText
                        });

                    }

                };

                xhttp.onerror = function () {
                };

            }

            xhttp.send();

        });

    }.bind(this)

    this._retrieve = function (dn) {

        return new Promise((accept, reject) => {
            let parmURL = `/navigator/retrieve?url=${encodeURIComponent(this.url)}&argument=${encodeURIComponent(dn)}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);
            xhttp.setRequestHeader("password", this.password);

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {
                    if (this.readyState === 4 && this.status === 200) {
                        var response = JSON.parse(this.responseText);

                        accept({
                            status: this.status,
                            response: response
                        });

                    } else {

                        reject({
                            status: this.status,
                            response: this.responseText
                        });

                    }

                };

                xhttp.onerror = function () {
                };

            }

            xhttp.send();

        });

    }.bind(this)

    this._export = function(dn) {

        return new Promise((accept, reject) => {
            let parmURL = `/navigator/export?url=${encodeURIComponent(this.url)}&dn=${encodeURIComponent(dn)}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);
            xhttp.setRequestHeader("password", this.password);

            xhttp.responseType = "blob";

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {

                    if (this.readyState === 4 && this.status === 200) {
                        accept(this.response);
                    } else {

                        reject({
                            status: this.status,
                            response: this.responseText
                        });

                    }

                };

                xhttp.onerror = function () {
                };

            }

            xhttp.send();

        });

    }.bind(this)

}

Message.prototype.connect = function () {

    return this._connect();

}


Message.prototype.status = function () {

    return this._status();

}

Message.prototype.search = function (argument, limit) {

    return this._search(argument, limit);

}

Message.prototype.next = function (argument, cursorPosition) {

    return this._next(argument, cursorPosition);

}

Message.prototype.retrieve = function (dn) {

    return this._retrieve(dn);

}

Message.prototype.export = function (dn) {

    return this._export(dn);

}
