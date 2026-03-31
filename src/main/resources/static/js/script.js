

function registerCustomer(event) {
    event.preventDefault(); // 🚫 stop page reload
	console.log("Form submitted");
    const data = {
        customerName: document.getElementById("name").value,
        customerEmail: document.getElementById("email").value,
        customerPhone: document.getElementById("phone").value
    };

    fetch("http://localhost:8082/customer/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
    .then(res => res.text()) // because backend returns String
    .then(msg => {
        alert("✅ " + msg);   // SUCCESS ALERT
    })
    .catch(err => {
        alert("❌ Something went wrong");
        console.error(err);
    });
}




function updateCustomer(event) {
    event.preventDefault(); // 🚫 stop reload

    const id = document.getElementById("updateId").value;

    const data = {
        customerName: document.getElementById("updateName").value,
        customerEmail: document.getElementById("updateEmail").value,
        customerPhone: document.getElementById("updatePhone").value
    };

    console.log("Updating customer:", id, data);

    fetch(`http://localhost:8082/customer/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
    .then(res => res.text())
    .then(msg => {
        alert("✅ " + msg);
    })
    .catch(err => {
        console.error(err);
        alert("❌ Update failed");
    });
}

function addAddress(event) {
    event.preventDefault();

    const data = {
        addressLine1: document.getElementById("line1").value,
        addressLine2: document.getElementById("line2").value,
        city: document.getElementById("city").value,
        state: document.getElementById("state").value,
        postalCode: document.getElementById("postalCode").value,
        customer: {
            customerId: document.getElementById("customerId").value
        }
    };

    console.log("Adding address:", data);

    fetch("http://localhost:8082/address", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
    .then(res => res.text())
    .then(msg => {
        alert("✅ " + msg);
    })
    .catch(err => {
        console.error(err);
        alert("❌ Failed to add address");
    });
}

function updateAddress(event) {
    event.preventDefault();

    const id = document.getElementById("addressId").value;

    const data = {
        addressLine1: document.getElementById("updateLine1").value,
        addressLine2: document.getElementById("updateLine2").value,
        city: document.getElementById("updateCity").value,
        state: document.getElementById("updateState").value,
        postalCode: document.getElementById("updatePostal").value,
        customer: {
            customerId: document.getElementById("updateCustomerId").value
        }
    };

    console.log("Updating address:", id, data);

    fetch(`http://localhost:8082/address/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
    .then(res => res.text())
    .then(msg => {
        alert("✅ " + msg);
    })
    .catch(err => {
        console.error(err);
        alert("❌ Failed to update address");
    });
}