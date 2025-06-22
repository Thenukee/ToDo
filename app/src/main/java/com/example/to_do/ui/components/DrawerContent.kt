package com.example.to_do.ui.components

class DrawerContent {





    @Composable
    fun DrawerContent(
        navController: NavController,
        closeDrawer: suspend () -> Unit
    ) {
        val ctx = LocalContext.current
        val wm = WorkManager.getInstance(ctx)

        ModalDrawerSheet {
            NavigationDrawerItem(
                label = { Text("Lists") },
                selected = false,
                onClick = { navController.navigate("lists"); ctx.launch { closeDrawer() } }
            )
            NavigationDrawerItem(
                label = { Text("Search") },
                selected = false,
                onClick = { navController.navigate("search"); ctx.launch { closeDrawer() } }
            )
            NavigationDrawerItem(
                label = { Text("Backup now") },
                selected = false,
                onClick = {
                    wm.enqueue(OneTimeWorkRequest.from(BackupWorker::class.java))
                    ctx.launch { closeDrawer() }
                }
            )
        }
    }

}


