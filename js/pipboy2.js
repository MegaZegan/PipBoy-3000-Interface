<script>
    // Function to update the current date
    function updateDate() {
        const date = new Date();
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        const formattedDate = date.toLocaleDateString('en-US', options); // Format as "Month Day, Year"
        document.getElementById('current-date').textContent = formattedDate;
    }

    // Function to update the current time
    function updateTime() {
        const date = new Date();
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        const formattedTime = `${hours}:${minutes}:${seconds}`; // Format as HH:mm:ss
        document.getElementById('current-time').textContent = formattedTime;
    }

    // Call the update functions when the page loads
    window.onload = function() {
        updateDate();
        updateTime();
        // Update time every second
        setInterval(updateTime, 1000);
    };
</script>
