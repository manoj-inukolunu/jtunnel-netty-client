$(document).ready(function () {
    $('#content-container').hide();
    console.log("Hello World");
    var table = $('#example').DataTable({
        "processing": true,
        "serverSide": true,
        "ajax": "/rest/data/history",
        "columns": [
            { "data": "requestId", "width": "85px" },
            { "data": "requestTime", "width": "150px" },
            {
                "data": "line",
                "width": "200px",
                "render": function (data, type, row) {
                    return data;
                }
            }
        ]
    });

    table.on('click', 'tbody tr', function () {
        var requestId = table.row(this).data().requestId;
        $.get("/rest/request/" + requestId).done(function (content) {
            $('#content').text(content);
        });
        $('#content-container').show();

        $('#responsePane').click(function () {
            $('#responsePane').addClass('active');
            $('#requestPane').removeClass('active');
            $.get("/rest/response/" + requestId).done(function (content) {
                $('#content').text(content);
            });
        });
        $('#requestPane').click(function () {
            $('#requestPane').addClass('active');
            $('#responsePane').removeClass('active');
            $.get("/rest/request/" + requestId).done(function (content) {
                $('#content').text(content);
            });
        });

        $('#replay').click(function () {
            $('#replay').addClass('active');
            $('#responsePane').removeClass('active');
            $('#requestPane').removeClass('active');
            $.get("/rest/replay/" + requestId).done(function (content) {
                $('#content').text(content);
            });
        });
    });

});


