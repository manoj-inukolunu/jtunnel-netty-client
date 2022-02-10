const sidebar = {
  components: {
    'tunnel-list': tunnelList
  },
  props: [], methods: {
    showModal(event) {
      $("#tunnel-modal").show();
    }
  }, template: `
   <div id="side-bar-1" class="sidebar bg-blue-800 text-blue-100 w-60 space-y-6 py-7 px-2 absolute inset-y-0 left-0 transform -translate-x-full md:relative md:translate-x-0 transition duration-200 ease-in-out">

    <a href="#" class="text-white flex items-center">
      <svg class="w-8 h-8 mx-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd"
              d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM6.293 6.707a1 1 0 010-1.414l3-3a1 1 0 011.414 0l3 3a1 1 0 01-1.414 1.414L11 5.414V13a1 1 0 11-2 0V5.414L7.707 6.707a1 1 0 01-1.414 0z"
              clip-rule="evenodd"/>
      </svg>
      <span class="text-2xl font-extrabold">JTunnel</span>
    </a>
    <button  class="bg-white hover:bg-gray-100 text-gray-800 font-semibold py-2 px-8 mx-7 border border-gray-400 rounded shadow w-40" @click="showModal" >
      New Tunnel
    </button>
    <nav>
      <tunnel-list></tunnel-list>
    </nav>
  </div>
  `
}