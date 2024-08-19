
(async function() {   // for this usecase here async is optional


    let input = {
        input: {
            item: "N/A"
        },
        fn: (function (i) {
            return "from async " + JSON.stringify(i);
        }).toString()
    }

    let a = callAsync(JSON.stringify({
        ...input,
        ...{
             input:{item:"AAA"}
           }
        })
    );

    let b = callAsync(JSON.stringify({
        ...input,
        ...{
             input:{item:"BBB"}
           }
        })
    );

    let c = callAsync(JSON.stringify({
        ...input,
        ...{
             input:{item:"CCC"}
           }
        })
    );

    return Promise.all([a,b,c]).then(
        result => {
            return "[\n" + result.join(",\n") + "\n]";
        },
    );

})