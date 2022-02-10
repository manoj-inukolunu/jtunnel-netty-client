const actionsDiv = {
  props: ['request'],
  reqeustId: '',
  editor: undefined,
  created() {
    $('#editor').hide();
    $('#request-header').hide();
    emitter.on('getRequestData', function (data) {
      requestId = data.requestId;
      $.get("/rest/request/" + requestId).done(function (content) {
        $('#editor').show();
        $('#request-header').show();
        editor.setValue(content);
        editor.clearSelection();
        editor.session.insert({row: 0, column: 0}, "\r\n");
      });
    })
  },
  mounted() {
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/monokai");
    // editor.session.setMode("ace/mode/java");
    editor.setReadOnly(true);
    editor.session.setUseWrapMode(true);
    $('#editor').hide();
    $('#request-header').hide();
  },
  methods: {
    replay() {
      $.get("/rest/replay/" + requestId).done(function (content) {
        editor.setValue(content);
      });
    },
    delete() {
      $.get("/rest/delete/" + requestId).done(function (content) {
        location.reload();
      });
    }
  },
  template: `
    <div class="flex-1">
        <header class="my-8 flex flex-col" id="request-header">
            <div class="flex px-6 items-center justify-between">
              <button @click="replay" class="bg-green-500 hover:bg-green-600 text-white font-semibold py-1 mx-2 border border-gray-400 rounded shadow w-40">Replay</button>
              <button class="bg-red-500 hover:bg-red-600  font-semibold py-1  border border-gray-400 text-white rounded shadow w-40">Delete</button>
            </div>
        </header>
        <div id="editor" class="h-3/4 text-base mx-5">
          
        </div>
    </div>
  `
}