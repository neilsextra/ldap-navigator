function Template(templateId) {

    this._template = document.querySelector(`script[data-template="${templateId}"]`).innerHTML;

};

/**
 * Value Substitution for templates
 * 
 * @param {*} values the values as a dictionary
 * @returns a string with substituted values that conform to the template
 */


Template.prototype.substitute = function(values) {
    let value = this._template;
    let keys = Object.keys(values);

    for (let key in keys) {
        value = value.split("${" + keys[key] + "}").join(values[keys[key]]);
    }

    return value;

}

/**
 * Value Substitution for templates
 * 
 * @param {*} elementId the element to append to
 * @param {*} values the values as a dictionary
 * @returns a string with substituted values that conform to the template
 */


Template.prototype.append = function(elementId, values) {
    let keys = Object.keys(values);
    let element = document.getElementById(elementId);

    let value = this.substitute(values);

    let fragment = document.createRange().createContextualFragment(value);

    element.innerHTML = "";
    element.appendChild(fragment);

}
