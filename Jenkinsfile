node {
    try {
        initialSettings {}

        buildApplication {}

        buildImage {
            services = [
                'realwave-sales-manager-command-application': 'realwave-sales-manager-command-application',
                'realwave-sales-manager-consumer': 'realwave-sales-manager-consumer',
                'realwave-sales-manager-query-application': 'realwave-sales-manager-query-application'
            ]
        }

        deployToArgo {
            microservices = [
                'realwave-sales-manager-command-application',
                'realwave-sales-manager-consumer',
                'realwave-sales-manager-query-application'
            ]
            chartsFolder = 'realwave'
        }

        tagRelease {}

    } catch (e) {
        throw e
    }
}