function Message() {

    this._connect = function (url) {
        
        return new Promise((accept, reject) => {
            let parmURL = `/navigator/connect?url=${encodeURIComponent(url)}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {
  
                    if (this.readyState === 4 && this.status === 200) {
                        var response = JSON.parse(this.responseText);
                        var result = JSON.parse(xhttp.response);

                        accept({
                            status: this.status,
                            response: response
                        });

                    } else {

                        reject({
                            status: this.status,
                            message: this.responseText
                        });

                    }

                };

                xhttp.onerror = function () {

                    alert("Unable to Login");

                };

            }

            xhttp.send();

        });

    }

    this._search = function (url, argument) {
        
        return new Promise((accept, reject) => {
            let parmURL = `/navigator/search?url=${encodeURIComponent(url)}&argument=${encodeURIComponent(argument)}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {

                    if (this.readyState === 4 && this.status === 200) {
                        var response = JSON.parse(this.responseText);
                        var result = JSON.parse(xhttp.response);

                         accept({
                            status: this.status,
                            response: response
                        });

                    } else {

                        reject({
                            status: this.status,
                            message: this.statusText
                        });

                    }

                };

                xhttp.onerror = function () {
                };

            }

            xhttp.send();

        });

    }

    this._retrieve = function (url, dn) {
        
        return new Promise((accept, reject) => {
            let parmURL = `/navigator/retrieve?url=${encodeURIComponent(url)}&argument=${encodeURIComponent(dn)}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {
                   if (this.readyState === 4 && this.status === 200) {
                        var response = JSON.parse(this.responseText);
                        var result = JSON.parse(xhttp.response);

                         accept({
                            status: this.status,
                            response: response
                        });

                    } else {

                        reject({
                            status: this.status,
                            message: this.statusText
                        });

                    }

                };

                xhttp.onerror = function () {
                };

            }

            xhttp.send();

        });

    }

    this._export = function (url, dn) {
        return new Promise((accept, reject) => {
            let parmURL = `/navigator/export?url=${encodeURIComponent(url)}&dn=${encodeURIComponent(dn)}`;
            var xhttp = new XMLHttpRequest();

            xhttp.open("GET", parmURL, true);
            xhttp.responseType = "blob";

            xhttp.onreadystatechange = async function () {
                xhttp.onload = function () {

                    if (this.readyState === 4 && this.status === 200) {
                        accept(this.response);
                    } else {

                        reject({
                            status: this.status,
                            message: this.statusText
                        });

                    }

                };

                xhttp.onerror = function () {
                };

            }

            xhttp.send();

        });

    }

}

Message.prototype.connect = function (url) {

    return this._connect(url.trim());

}

Message.prototype.search = function (url, argument) {

    return this._search(url.trim(), argument);

}

Message.prototype.retrieve = function (url, dn) {

    return this._retrieve(url.trim(), dn);

}

Message.prototype.export = function (url, dn) {

    return this._export(url.trim(), dn);

}
