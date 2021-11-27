$(document).ready(function () {
    $('#content-container').hide();
    var table = $('#example').DataTable({
        "select": true,
        "processing": true,
        "serverSide": true,
        "ajax": "/rest/data/history",
        "initComplete": function() {
                             $('.dataTables_filter input').unbind();
                             $('.dataTables_filter input').bind('keyup', function(e){
                                 var code = e.keyCode || e.which;
                                 if (code == 13) {
                                     table.search(this.value).draw();
                                 }
                             });
                         },
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

    $('#example tbody').on('click', 'tr', function () {
        $(this).toggleClass('selected');
    });

    function removeAllClasses() {
        $('#nav-pills-id li a').each(function () {
            $(this).removeClass('active');
        });
    }

    table.on('click', 'tbody tr', function () {
        var requestId = table.row(this).data().requestId;
        $.get("/rest/request/" + requestId).done(function (content) {
            $('#content').text(content);
        });
        $('#content-container').show();

        $('#responsePane').click(function () {
            removeAllClasses();
            $('#responsePane').addClass('active');
            $.get("/rest/response/" + requestId).done(function (content) {
                $('#content').text(content);
            });
        });
        $('#requestPane').click(function () {
            removeAllClasses();
            $('#requestPane').addClass('active');
            $.get("/rest/request/" + requestId).done(function (content) {
                $('#content').text(content);
            });
        });

        $('#replay').click(function () {
            removeAllClasses();
            $('#replay').addClass('active');
            $.get("/rest/replay/" + requestId).done(function (content) {
                $('#content').text(content);
            });
        });

        $('#delete').click(function () {
            removeAllClasses();
            $('#delete').addClass('active');
            $.get("/rest/delete/" + requestId).done(function (content) {
                location.reload();
            });
        });
    });

});




