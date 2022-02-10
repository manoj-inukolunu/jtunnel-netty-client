const app = Vue.createApp({
  components: {
    'side-bar': sidebar,
    'tunnelList': tunnelList,
    'content-div': contentDiv,
    'actions-div': actionsDiv
  }
})
app.mount("#container")
