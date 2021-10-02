

const mainApp = {
  mounted() {

    fetch('http://localhost:5050/rest/history?start=0&end=10')
      .then(res => res.json())
      .then(data => {
        this.total = data.total;
        this.countPerPage = data.countPerPage;
        this.pages = Math.floor(data.total / data.countPerPage);
        this.history = data.data;
        this.currPage = 1;
      })
  },
  prevListElem: null,
  currentRequestId: null,
  currPage: 1,
  countPerPage: 0,
  total: 0,
  data() {
    return {
      pages: null,
      total: null,
      history: [],
      request: null,
      response: null,
      showRequest: false,
      showResponse: false,
      reqClass: null,
      resClass: null,
      listActive: null,
      repClass: null
    }
  },
  methods: {
    load(item, evt) {
      if (this.prevListElem != null) {
        this.prevListElem.className = this.prevListElem.className.replace(" active", "");
      } else {
        this.prevListElem = evt.target;
      }
      evt.target.className += " active";
      this.prevListElem = evt.target;
      this.currentRequestId = item.requestId;
      fetch('http://localhost:5050/rest/request/' + item.requestId)
        .then(response => response.text())
        .then(data => this.request = data);
      fetch('http://localhost:5050/rest/response/' + item.requestId)
        .then(resp => resp.text()).then(data => this.response = data);
      this.funcShowRequest();
    },
    funcShowRequest() {
      this.showRequest = true;
      this.reqClass = "active";
      this.resClass = null;
      this.showResponse = false;
    },
    funcShowResponse() {
      this.showResponse = true;
      this.showRequest = false;
      this.reqClass = null;
      this.resClass = "active";
    },
    funcReplay(item) {
      this.repClass = "active";
      this.reqClass = null;
      this.resClass = null;
      fetch('http://localhost:5050/rest/replay/' + this.currentRequestId)
        .then(response => window.location.reload());
    },
    loadPage(page, evt) {
      var start = this.countPerPage * page;

      var end = start + this.countPerPage;
      this.clearAll();
      fetch('http://localhost:5050/rest/history?start=' + start + '&end=' + end)
        .then(res => res.json())
        .then(data => {
          this.total = data.total;
          this.pages = Math.floor(data.total / data.countPerPage);
          this.history = data.data;
        });
      this.currPage = page;

    },
    clearAll() {
      if (this.prevListElem != null) {
        this.prevListElem.classList.remove("active");
      }

      this.request = null;
      this.response = null;
      this.showRequest = false;
      this.showResponse = false;
      this.reqClass = null;
      this.resClass = null;
      this.listActive = null;
      this.repClass = null;
      this.prevListElem = null;
      this.currentRequestId = null;
    }

  }
}


Vue.createApp(mainApp).mount('#app')