(function () {
    angular
        .module("PlagiarismDetector")
        .controller("RegisterController", registerController);

    function registerController($location, UserService) {
        var vm = this;
        vm.createUser = createUser;

        function createUser(user) {
            if(user && user.password && user.password2 && user.email)  {
                if (user.password === user.password2) {
                    UserService
                        .createUser(user)
                        .then(function (user) {
                            if(user.role === 1) {
                                $location.url('/profile/'+user.id);
                            }else{
                                $location.url('/coursestaff/'+user.id);
                            }
                        }, function (err) {
                            vm.error = err.data.message;
                        });
                } else
                    vm.error = "passwords do not match";

            }else
                vm.error = "Please fill all details.";

        }
    }
})();