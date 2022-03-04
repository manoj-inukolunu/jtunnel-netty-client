const emitter = mitt()
const contentDiv = {
  props: [], request: undefined, data() {
    return {contentList: undefined, recordsTotal: undefined, pageNum: undefined}
  }, mounted() {
    fetch('/rest/history?start=0&end=10').then(res => res.json()).then(d => {
      console.log(d);
      this.contentList = d.data;
      this.recordsTotal = d.recordsTotal;
      this.pageNum = 1;
    })
  }, components: {
    'actions-div': actionsDiv
  }, methods: {
    prev(event) {

      if (this.pageNum > 1) {
        let start = (this.pageNum - 1) * 10;
        end = start + 10;
        fetch('/rest/history?start=' + start + '&end=' + end).then(
            res => res.json()).then(d => {
          console.log(d);
          this.contentList = d.data;
          this.recordsTotal = d.recordsTotal;
          this.pageNum = this.pageNum - 1;
        })
      }
    }, next(event) {
      if (this.pageNum !== Number(this.recordsTotal / 10)) {

        let start = Number((this.pageNum - 1) * 10);
        end = start + 10;
        fetch('/rest/history?start=' + start + '&end=' + end).then(
            res => res.json()).then(d => {
          console.log(d);
          this.contentList = d.data;
          this.recordsTotal = d.recordsTotal;
          this.pageNum++;
        })
      }
    }, loadData(event) {
      let elem = event.target;
      while (elem.tagName !== 'TR') {
        elem = elem.parentElement
      }
      emitter.emit('getRequestData', {requestId: elem.id})
    }, close(event) {
      $("#tunnel-modal").hide();
    }, create(event) {
      $.ajax({
        type: "POST",
        url: "/tunnel/create",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
          subdomain: $("#tunnelSubdomain").val(),
          tunnelName: $("#tunnelName").val(),
          port: $("#localServerPort").val()
        }),
        complete: function (jqXHR) {
          $("tunnel-modal").hide();
          location.reload();
        },
        done: function () {
          $("tunnel-modal").hide();
          location.reload();
        }
      });
    }
  }, template: `


<!-- This example requires Tailwind CSS v2.0+ -->
    <div class="fixed z-10 inset-0 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true"  hidden id ="tunnel-modal">
  <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
    <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true"></div>

    <!-- This element is to trick the browser into centering the modal contents. -->
    <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>

    
    <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
      <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
        <div >
          <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
            <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
              Create Tunnel
            </h3>
            <div class="block p-6">
  <form>
    <div class="form-group mb-6">
      <label for="exampleInputEmail1" class="form-label inline-block mb-2 text-gray-700">Tunnel Name</label>
      <input type="email" class="form-control
        block
        w-full
        px-3
        py-1.5
        text-base
        font-normal
        text-gray-700
        bg-white bg-clip-padding
        border border-solid border-gray-300
        rounded
        transition
        ease-in-out
        m-0
        focus:text-gray-700 focus:bg-white focus:border-blue-600 focus:outline-none" id="tunnelName"
        aria-describedby="emailHelp" placeholder="Name">
     
    </div>
    <div class="form-group mb-6">
      <label for="exampleInputEmail1" class="form-label inline-block mb-2 text-gray-700"> Subdomain</label>
      <input type="email" class="form-control
        block
        w-full
        px-3
        py-1.5
        text-base
        font-normal
        text-gray-700
        bg-white bg-clip-padding
        border border-solid border-gray-300
        rounded
        transition
        ease-in-out
        m-0
        focus:text-gray-700 focus:bg-white focus:border-blue-600 focus:outline-none" id="tunnelSubdomain"
        aria-describedby="emailHelp" placeholder="Name">
     
    </div>
    <div class="form-group mb-6">
      <label for="exampleInputPassword1" class="form-label inline-block mb-2 text-gray-700">LocalServer Port</label>
      <input type="text" class="form-control block
        w-full
        px-3
        py-1.5
        text-base
        font-normal
        text-gray-700
        bg-white bg-clip-padding
        border border-solid border-gray-300
        rounded
        transition
        ease-in-out
        m-0
        focus:text-gray-700 focus:bg-white focus:border-blue-600 focus:outline-none" id="localServerPort"
        placeholder="Port ">
    </div>
  </form>
</div>
          </div>
        </div>
      </div>
      <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
        <button type="button" @click="create" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-green-600 text-base font-medium text-white hover:bg-green-700  sm:ml-3 sm:w-auto sm:text-sm">
          Create
        </button>
        <button type="button" @click="close" class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
          Cancel
        </button>
      </div>
    </div>
  </div>
</div>
    <div class="container flex-1 py-6">

    <div class="my-2 px-10 ">
      <div class="block relative">
                    <span class="h-full absolute inset-y-0 left-0 flex items-center pl-2">
                        <svg viewBox="0 0 24 24" class="h-4 w-4 fill-current text-gray-500">
                            <path
                                d="M10 4a6 6 0 100 12 6 6 0 000-12zm-8 6a8 8 0 1114.32 4.906l5.387 5.387a1 1 0 01-1.414 1.414l-5.387-5.387A8 8 0 012 10z">
                            </path>
                        </svg>
                    </span>
        <input placeholder="Search"
               class="appearance-none rounded-r rounded-l sm:rounded-l-none border border-gray-400 border-b block pl-8 pr-6 py-2 w-full bg-white text-sm placeholder-gray-400 text-gray-700 focus:bg-white focus:placeholder-gray-600 focus:text-gray-700 focus:outline-none"/>
      </div>
    </div>
      
      <div class="flex flex-col w-full px-3 py-5">
      <div class="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-2">
        <div class="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
          <div class="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Request Data
                </th>
              </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="data in contentList" :key="data.requestId" :id="data.requestId" class="hover:bg-gray-100 cursor-pointer focus:bg-gray-300 py-3" @click=loadData>
                <td class="px-4 py-3 whitespace-nowrap">
                  <div class="text-sm text-gray-900"> {{data.line}}</div>
                  <div class="text-sm text-gray-500">Time: {{data.requestTime}}</div>
                </td>
              </tr>
    
              </tbody>
            </table>
            <div
            class="px-5 py-5 bg-white border-t flex flex-col xs:flex-row items-center xs:justify-between">
                        <span class="text-xs xs:text-sm text-gray-900">
                            Showing Page {{pageNum}} of {{recordsTotal}} Entries
                        </span>
          <div class="inline-flex mt-2 xs:mt-0">
            <button
                class="text-sm bg-gray-300 hover:bg-gray-400 text-gray-800 font-semibold py-2 px-4 rounded-l" @click="prev">
              Prev
            </button>
            <button
                class="text-sm bg-gray-300 hover:bg-gray-400 text-gray-800 font-semibold py-2 px-4 rounded-r" @click="next">
              Next
            </button>
          </div>
        </div>
          </div>
        </div>
      </div>
      </div>
    </div>
    <actions-div></actions-div>
  `
}