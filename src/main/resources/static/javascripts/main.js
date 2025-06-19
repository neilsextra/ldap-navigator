'use strict'

var tableView = null;
var attributes = null;

var cursorPosition = null;

var MAX_ITEMS = 99;

function showError(message) {

    document.getElementById("error-message").innerHTML = message;
    document.getElementById("error-dialog").showModal();

}

function hex2Char(value) {
    const hexToByte = (hex) => {
        var value = parseInt(`0x${hex}`, 16)
        var output = value >= 32 && value <= 127 ? String.fromCharCode(value) : ".";

        return output;

    }

    var hex = [];

    for (var iChar = 0; iChar < value.length; iChar += 2) {

        hex.push(value.substring(iChar, iChar + 2));

    }

    var output = "";

    for (var iHex = 0; iHex < hex.length; iHex++) {
        output += `${hexToByte(hex[iHex])}`;
    }

    return output;

}

function copyToClipboard(type, value) {

    function formatHex(value) {

        const hexToByte = (hex) => {
            var value = parseInt(`0x${hex}`, 16)
            var output = value >= 32 && value <= 127 ? String.fromCharCode(value) : ".";

            return output;

        }

        var hex = [];

        for (var iChar = 0; iChar < value.length; iChar += 2) {

            hex.push(value.substring(iChar, iChar + 2));

        }

        var hexValues = "";
        var charValues = "";

        var iHex = 0;
        var iPos = 0

        var output = "";

        for (; iHex < hex.length; iHex++) {

            iPos += 1;

            hexValues += `${hex[iHex]}|`;
            charValues += `${hexToByte(hex[iHex])}`;

            if (iPos % 16 == 0) {
                output += hexValues;

                output += charValues;

                output += "\n";

                hexValues = "";
                charValues = "";
            }

        }

        if (iHex % 16 != 0) {
            output += hexValues;

            for (var iCount = 0; iPos % 16 != 0; iPos++, iCount++) {

                output += `${iCount < 16 ? "   " : "|"}`;
            }

            output += charValues;

            output += ``;
        }

        return output;

    }

    var decodedValue = atob(value);

    if (type == "Binary") {
        navigator.clipboard.writeText(formatHex(decodedValue));
    } else {
        navigator.clipboard.writeText(decodedValue.replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
    }

}

function copyToSearch(type, value) {

    document.getElementById("search-argument").value = (type == "Binary") ? hex2Char(value).split('#')[0] : value.split('#')[0];

}

function copyToLaunch(type, value) {

    document.getElementById("search-argument").value = (type == "Binary") ? hex2Char(value).split('#')[0] : value.split('#')[0];

    search(document.getElementById("search-argument").value);

}

function setup(container, table) {
    var storage = window.localStorage.getItem(`${container}:${window.storageKey}`);

    var table = document.getElementById(table);

    if (storage != null) {
        var searchHistory = JSON.parse(storage);

        for (var iHistory = 0; iHistory < searchHistory.length; iHistory++) {
            var row = table.insertRow();

            row.setAttribute("onclick", `window.select('${searchHistory[iHistory]}')`, 0);

            var cell = row.insertCell();

            cell.className = "result-table-item";
            cell.style.textWrap = "nowrap";
            cell.style.whiteSpace = "nowrap";

            cell.innerHTML = searchHistory[iHistory];
        }

    }

}

async function search(dn) {
    document.getElementById("wait-dialog").showModal();

    try {
        var message = new Message();
        var result = await message.search(window.ldapURL, dn);

        var html = "<table class='result-table'>";
        var items = 0;

        for (var dn in result.response.results) {

            html += `<tr onclick="window.select('${result.response.results[dn]}')">` +
                `<td class='result-table-item' style="white-space: nowrap; text-wrap: nowrap;">` +
                `${result.response.results[dn]}</td></tr>`;
            items += 1;

        }

        if (result.response.cursorPosition.length > 0) {
            cursorPosition = result.response.cursorPosition;
            document.getElementById("search-navigate-forward").disabled = false;
        } else {
            cursorPosition = null;

            if (items < MAX_ITEMS - 1) {
                document.getElementById("search-navigate-forward").disabled = true;
            } else {
                document.getElementById("search-navigate-forward").disabled = false;
            }

        }

        document.getElementById("search-navigate-refresh").disabled = false;

        document.getElementById("search-navigate-dn").textContent = result.response.dn;
        document.getElementById("search-results").innerHTML = html;

        document.getElementById("wait-dialog").close();

    } catch (exception) {

        showError(`Server Error: ${exception.response}`);

        document.getElementById("wait-dialog").close();

    }

}

async function forward(dn, cursorPosition) {

    document.getElementById("wait-dialog").showModal();

    var message = new Message();
    var result = await message.next(window.ldapURL, dn, cursorPosition);

    var html = "<table class='result-table'>";

    var items = 0;

    for (var dn in result.response.results) {

        html += `<tr onclick="window.select('${result.response.results[dn]}')">` +
            `<td class='result-table-item' style="white-space: nowrap; text-wrap: nowrap;">` +
            `${result.response.results[dn]}</td></tr>`;

        items += 1;

    }

    if (items < MAX_ITEMS) {
        document.getElementById("search-navigate-forward").disabled = true;
    }

    cursorPosition = result.response.cursorPosition;

    document.getElementById("search-navigate-dn").textContent = result.response.dn;
    document.getElementById("search-results").innerHTML = html;

    document.getElementById("wait-dialog").close();

}

async function backward() {

    rowCount -= 1000;

    rowCount = await getSearchResults(rowCount)

    if (rowCount <= 1000) {
        document.getElementById("search-navigate-left").disabled = true;
    }

}

function find(table, dn) {

    for (var iRow = 0, row; row = table.rows[iRow]; iRow++) {

        for (var iCell = 0, col; col = row.cells[iCell]; iCell++) {

            if (dn.trim() == col.innerText.trim()) {
                return true;
            }
        }
    }

    return false;

}

function remove(container, tableID) {

   var containerStorage = window.localStorage.getItem(`${container}:${window.storageKey}`);
    var entries = containerStorage != null ? JSON.parse(containerStorage) : [];
    var selectedDN = document.getElementById("selected-dn").innerText;

    if (selectedDN.length == 0) {
        return;
    }

    var filteredEntries = entries.filter(item => item !== document.getElementById("selected-dn").innerText);

    window.localStorage.setItem(`${container}:${selectedDN}`, JSON.stringify(filteredEntries));

    var table = document.getElementById(tableID);

    var item = -1;

    exit: for (var iRow = 0, row; row = table.rows[iRow]; iRow++) {

        for (var iCell = 0, col; col = row.cells[iCell]; iCell++) {

            if (selectedDN.trim() == col.innerText.trim()) {
                item = iRow;
                break exit;
            }
        }
    }

    if (item != -1) {
        table.deleteRow(item);
    }

}

function filter(container, tableView, filter) {
    var storage = window.localStorage.getItem(`${container}:${window.storageKey}`);

    if (storage != null) {
        var entries = JSON.parse(storage);

        var table = document.getElementById(tableView);

        table.innerHTML = "";

        for (var iEntry = 0; iEntry < entries.length; iEntry++) {

            if (entries[iEntry].toLowerCase().indexOf(document.getElementById(filter).value.toLowerCase()) != -1) {
                var row = table.insertRow();

                row.setAttribute("onclick", `window.select('${entries[iEntry]}')`, 0);

                var cell = row.insertCell();

                cell.className = "result-table-item";
                cell.style.textWrap = "nowrap";
                cell.style.whiteSpace = "nowrap";

                cell.innerHTML = entries[iEntry];

            }

        }

    }

}
async function showAttributes(result) {

    function filter(filterType, filterSelection, entry) {

        if (filterSelection.length == 0) {
            return true;
        } else if (filterType == "name" && entry["name"].trim().toLowerCase().indexOf(filterSelection.trim().toLowerCase()) != -1) {
            return true;
        } else if (filterType == "oid" && entry["oid"].trim().toLowerCase().indexOf(filterSelection.trim().toLowerCase()) != -1) {
            return true;
        } else if (filterType == "value" && entry["value"].trim().toLowerCase().indexOf(filterSelection.trim().toLowerCase()) != -1) {
            return true;
        }

        return false;

    }

    if (result == null) {
        return;
    }

    var columns = ["Attribute", "Object-ID", "Syntax", "Type", "Data"];

    var filterTypeOptions = document.getElementById("filter-type");
    var filterType = filterTypeOptions.options[filterTypeOptions.selectedIndex].value;
    var filterSelection = document.getElementById("filter-selection").value;
    var rows = [];
    var timestamps = [];

    for (var entry in result.response) {
        var row = [];

        if (filter(filterType, filterSelection, result.response[entry])) {

            for (var field in result.response[entry]) {

                row.push(result.response[entry][field].replace(/</g, '&lt;').replace(/>/g, '&gt;'));

            }

            rows.push(row)

        }

    }

    var dataview = new DataView(columns, rows);
    let painter = new Painter();

    let widths = [];

    widths.push(250);
    widths.push(250);
    widths.push(250);
    widths.push(80);
    widths.push(800);

    tableView = new TableView({
        "container": "#artifacts-container",
        "model": dataview,
        "nbRows": dataview.Length,
        "rowHeight": 30,
        "headerHeight": 30,
        "painter": painter,
        "columnWidths": widths
    });

    document.getElementById('artifacts-container').style.display = "inline-block";

    window.setTimeout(function () {
        tableView.setup();
        tableView.resize();
    }, 10);

    tableView.addProcessor(async function (button, row, x, y) {

        function appendHex(value) {

            const hexToByte = (hex) => {

                var value = parseInt(`0x${hex}`, 16)
                var output = value >= 32 && value <= 127 ? String.fromCharCode(value) : ".";

                return output;

            }

            var hex = [];
            for (var iChar = 0; iChar < value.length; iChar += 2) {

                hex.push(value.substring(iChar, iChar + 2));

            }

            var hexValues = "";
            var charValues = "";

            var iHex = 0;
            var iPos = 0

            var html = `<table class="hex">`;
            html += `</tr>`;

            for (; iHex < hex.length; iHex++) {

                iPos += 1;

                hexValues += `<td>${hex[iHex]}&nbsp|&nbsp</td>`;
                charValues += `<td>${hexToByte(hex[iHex])}</td>`;

                if (iPos % 16 == 0) {
                    html += hexValues;

                    html += charValues;


                    html += "</tr><tr>";

                    hexValues = "";
                    charValues = "";
                }

            }

            if (iHex % 16 != 0) {
                html += hexValues;

                for (var iCount = 0; iPos % 16 != 0; iPos++, iCount++) {

                    html += `<td>${iCount < 16 ? "&nbsp" : ""}&nbsp;&nbsp;|&nbsp;</td>`;
                }

                html += charValues;

                html += `</tr>`;
            }

            html += `</table>`;

            html += `</div>`;

            return html;

        }

        document.getElementById("artifact-view").style.display = "inline-block";
        document.getElementById("artifact-entry-attribute").innerHTML =
            `Attribute: <b>${rows[row][0]}</b>` +
            `&nbsp;&nbsp;` +
            `<div style="position:absolute; top:5px; right:5px; height:32px;"> ` +
            `<button class="button-no-style" onclick="window.copyToClipboard('${rows[row][3]}', '${btoa(rows[row][4])}')">` +
            `<img src="images/clipboard.svg" width="18", height="18"></img> </button>` +
            `&nbsp;` +
            `<button class="button-no-style" onclick="window.copyToSearch('${rows[row][3]}', '${rows[row][4]}')">` +
            `<img src="images/pen-to-square.svg" width="18", height="18"></img> </button>` +
            `&nbsp;` +
            `<button class="button-no-style" onclick="window.copyToLaunch('${rows[row][3]}', '${rows[row][4]}')">` +
            `<img src="images/launch.svg" width="18", height="18"></img> </button>` +
            `</div>`;

        if (rows[row][3] == "String") {
            var fragment = document.createRange().createContextualFragment(`<div class="hex">${rows[row][4].replace(/</g, '&lt;').replace(/>/g, '&gt;')}</div>`);

            document.getElementById("artifact-entry-view").innerHTML = "";
            document.getElementById("artifact-entry-view").appendChild(fragment);
        } else {
            document.getElementById("artifact-entry-view").innerHTML = appendHex(rows[row][4]);
        }

    });

}

async function select(dn) {

    document.getElementById("selected-dn").innerHTML = `${dn}`;
    document.getElementById("wait-dialog").showModal();

    var message = new Message();

    try {

        attributes = await message.retrieve(window.ldapURL, dn);

        showAttributes(attributes);

        var historyStorage = window.localStorage.getItem(`history:${window.storageKey}`);

        var searchHistory = historyStorage != null ? JSON.parse(historyStorage) : [];

        var table = document.getElementById("history-table");

        if (!find(table, dn)) {
            var row = table.insertRow();

            row.setAttribute("onclick", `window.select('${dn}')`, 0);

            var cell = row.insertCell();

            cell.className = "result-table-item";
            cell.style.textWrap = "nowrap";
            cell.style.whiteSpace = "nowrap";

            cell.innerHTML = dn;

            searchHistory.push(dn);

            window.localStorage.setItem(`history:${window.storageKey}`, JSON.stringify(searchHistory));

        }

        document.getElementById("dn-download-button").disabled = false;
        document.getElementById("unbookmark-button").disabled = false;
        document.getElementById("bookmark-button").disabled = false;
        document.getElementById("wait-dialog").close();

    } catch (exception) {

        showError(`Server Error: ${exception.response}`);

        document.getElementById("wait-dialog").close();
    }


}

/**
 * Respond to the Document 'ready' event
 */
window.onload = async function () {

    window.ldapURL = "";
    window.storageKey = "";

    var closeButtons = document.getElementsByClassName("close-button");

    for (var closeButton = 0; closeButton < closeButtons.length; closeButton++) {

        closeButtons[closeButton].addEventListener('click', (e) => {

            document.getElementById(e.target.id.replace(/close\-|cancel\-/, "")).close();

        });

    }

    document.getElementById("ok-connect-dialog").addEventListener('click', async (e) => {
        var message = new Message();

        try {

            document.getElementById("wait-dialog").showModal();

            var urlParts = document.getElementById("ldap-url").value.split("@");
            var ldapUrl = urlParts[0] + ":" + document.getElementById("ldap-password").value + "@" + urlParts[1];

            var result = await message.connect(ldapUrl);

            document.getElementById("connect-dialog").close();
            window.storageKey = result.response.host + ":" + result.response.port + "@" + result.response.username;
            document.getElementById("viewer-status").innerHTML = `<b>Connected:&nbsp;</b>${window.storageKey}`;
            window.ldapURL = ldapUrl;

            setup("history", "history-table");
            setup("bookmarks", "bookmarks-table");

            document.getElementById("wait-dialog").close();

        } catch (exception) {
            showError(`Server Error: ${exception.response}`);
            document.getElementById("wait-dialog").close();
        }

    });

    document.getElementById("search-button").addEventListener('click', (e) => {

        search(document.getElementById("search-argument").value);

    });

    document.getElementById("search-navigate-refresh").addEventListener('click', (e) => {

        search(document.getElementById("search-navigate-dn").textContent);

    });

    document.getElementById("search-navigate-forward").addEventListener('click', (e) => {

        forward(document.getElementById("search-navigate-dn").textContent, cursorPosition);

    });

    document.getElementById("filter-button").addEventListener('click', (e) => {

        showAttributes(attributes);

    });

    document.getElementById("filter-history-button").addEventListener('click', (e) => {

        filter("history", "history-table", "filter-history");

    });

    document.getElementById("filter-bookmarks-button").addEventListener('click', (e) => {

        filter("bookmarks", "bookmarks-table", "filter-bookmarks");

    });

    document.getElementById("bookmark-button").addEventListener('click', async (e) => {
        var bookmarkStorage = window.localStorage.getItem(`bookmarks:${window.storageKey}`);
        var bookmarks = bookmarkStorage != null ? JSON.parse(bookmarkStorage) : [];

        var found = false;

        exit: for (var bookmark in bookmarks) {


            if (bookmarks[bookmark] == document.getElementById("selected-dn").innerText) {

                found = true;

                break exit;

            }

        }

        if (!found) {
            bookmarks.push(document.getElementById("selected-dn").innerText);

            window.localStorage.setItem(`bookmarks:${window.storageKey}`, JSON.stringify(bookmarks));

            var table = document.getElementById("bookmarks-table");

            var row = table.insertRow();

            row.setAttribute("onclick", `window.select('${document.getElementById("selected-dn").innerText}')`, 0);

            var cell = row.insertCell();

            cell.className = "result-table-item";
            cell.style.textWrap = "nowrap";
            cell.style.whiteSpace = "nowrap";

            cell.innerHTML = document.getElementById("selected-dn").innerText;


        }

    });

    document.getElementById("unbookmark-button").addEventListener('click', async (e) => {

        remove("bookmarks", "bookmarks-table");

    });

    document.getElementById("delete-bookmark-button").addEventListener('click', async (e) => {

        remove("bookmarks", "bookmarks-table");

    });

    document.getElementById("dn-download-button").addEventListener('click', async (e) => {
        var fileUtil = new FileUtil(document);
        var message = new Message();

        var blob = await message.export(window.ldapURL,
            document.getElementById("selected-dn").innerText);

        fileUtil.saveAs(blob, document.getElementById("selected-dn").innerText + ".asn1");

    });

    document.getElementById("clear-history-button").addEventListener('click', async (e) => {

        window.localStorage.removeItem(`history:${window.storageKey}`);

        document.getElementById("history-table").innerHTML = "";

    });

    document.getElementById("clear-bookmarks-button").addEventListener('click', async (e) => {

        window.localStorage.removeItem(`bookmarks:${window.storageKey}`);

        document.getElementById("bookmarks-table").innerHTML = "";

    });

    document.getElementById("delete-history-button").addEventListener('click', async (e) => {

          remove("history", "history-table");

    });

    document.getElementById("erase-button").addEventListener('click', async (e) => {

        document.getElementById("search-argument").value = "";
        document.getElementById("search-button").disabled = true;

    });

    document.getElementById("erase-filter-button").addEventListener('click', async (e) => {

        document.getElementById("filter-selection").value = "";

    });

    document.getElementById("erase-bookmarks-filter-button").addEventListener('click', async (e) => {

        document.getElementById("filter-bookmarks").value = "";

    });

    document.getElementById("erase-history-filter-button").addEventListener('click', async (e) => {

        document.getElementById("filter-history").value = "";

    });


    document.getElementById("erase-history-filter-button").addEventListener('click', async (e) => {

        document.getElementById("filter-history").value = "";

    });

    document.getElementById("connect-dialog").addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && preventClosing) {
            event.preventDefault();
        }
    });

    document.getElementById("wait-dialog").addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && preventClosing) {
            event.preventDefault();
        }
    });


    document.getElementById("search-argument").addEventListener('input', (event) => {

        document.getElementById("search-button").disabled = document.getElementById("search-argument").value.length < 1;

    });

    document.getElementById("connect-button").addEventListener('click', async (e) => {

        document.getElementById("cancel-connect-dialog").style.visibility = "visible";
        document.getElementById("connect-dialog").showModal();

    });

    document.getElementById("connect-dialog").showModal();

    activateTabs('tabs', 'search-panel', 'tab1');

}
