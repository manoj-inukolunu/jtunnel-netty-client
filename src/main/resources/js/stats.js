const mainApp = {
  mounted() {
    fetch('http://localhost:5050/rest/history')
      .then(res => res.json())
      .then(data => this.history = data)
  },
  prevListElem:null,
  data() {
    return {
      history: [],
      request: null,
      response: null,
      showRequest: false,
      showResponse: false,
      reqClass:null,
      resClass:null,
      listActive:null
    }
  },
  methods: {
    load(item,evt) {
       if(this.prevListElem!=null){
         this.prevListElem.className= this.prevListElem.className.replace(" active","");
       }else{
        this.prevListElem = evt.target;
       }
       evt.target.className+=" active";
       this.prevListElem = evt.target;
       fetch('http://localhost:5050/rest/request/'+item.requestId)
             .then(response=>response.text())
             .then(data => this.request = data);
       fetch('http://localhost:5050/rest/response/'+item.requestId)
       .then(resp => resp.text()).then(data => this.response = data);
      this.funcShowRequest();
    },
    funcShowRequest(){
      this.showRequest = true;
      this.reqClass="active";
      this.resClass = null;
      this.showResponse = false;
    },
    funcShowResponse(){
      this.showResponse = true;
      this.showRequest = false;
      this.reqClass=null;
      this.resClass="active"
    }
  }
}

Vue.createApp(mainApp).mount('#app')