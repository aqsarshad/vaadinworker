# Purpose #
VaadinWorker is an addon for Vaadin that eases the way how long-running tasks are executed in background-threads and their results are communicated to the client.

# Installation #
For using VaadinWorker you roughly need to follow these steps:

1. Put `vaadinworker.jar` in your `WEB-INF/lib-folder`
2. Implement an `ExecutorServiceProvider`. More details to this step below. For Servlet-enviroments there is a ready-made solution that gets shipped with the addon.
3. Place the `BackgroundExecutor` anywhere on the view you want to update via a background-thread. Note that it must be visible as long as backgound-tasks are running, otherwise the updates to the view won't become visible either. The component will hide itself as soon as there are no more background-tasks, so don't worry too much about your layout.
4. Create at least one `VaadinWorker`. More details about that follow below.
5. Submit your `VaadinWorker`(s) to the `BackgroundExecutor` using it's `submit(VaadinWorker)`-method.

# Usage #
## Implementing an `ExecutorServiceProvider` ##
An `ExecutorServiceProvider` is responsible for giving the `BackgroundExecutor` access to an `ExecutorService`. Such an `ExecutorService` might for example be accessible via JNDI. The interface itself is as easy as possible, it has only one method: `ExecutorService getExecutorService(Application)`.

The method gets the `Application` for which an `ExecutorService` should be retrieved as an argument. This can be used for example to distinguish between several threadpools that are accessible via a cross-`Application`-resource like the `ServletContext`. As a return-value you must provide the `ExecutorService` the `BackgroundExecutor` should use for executing the submitted `VaadinWorker`s.

Note that the `ExecutorServiceProvider` is asked frequently to provide an `ExecutorService` (at least every time a `VaadinWorker`gets submitted), so that the method `getExecutorService()` should return rather quick.

### `ExecutorServiceProvider` in a Servlet-Environment ###
For Servlet-Environments the addon already delivers a ready-made `ExecutorServiceProvider`. That one creates an `ExecutorServiceProvider` on startup of the Web-application. If the Web-application shuts down, also the created `ExecutorService` will be destroyed. The `ExecutorService` gets saved in the `ServletContext` and will be retrieved from there every time `getExecutorService()` gets called.

Too install this mechanism you need to add the following lines to your `web.xml`:
```
	<context-param>
		<param-name>threadCount</param-name>
		<param-value>20</param-value>
	</context-param>
	<context-param>
		<param-name>gracefulShutdown</param-name>
		<param-value>false</param-value>
	</context-param>
	
	<listener>
		<listener-class>
			de.tobiasdemuth.vaadinworker.executorserviceprovider.ExecutorServiceContextListener
		</listener-class>
	</listener>
```
The `threadCount`-context-parameter controls how many threads will be in the fixed-size-threadpool that will be created. With the `boolean`-parameter `gracefulShutdown` you can control wether tasks will be simply interrupted when the threadpool shuts down or if the tasks will be allowed to end normally. Both parameters are optional. If `threadCount`is missing, a `SingleThreadExecutor` will be created executing all your `VaadinWorker`s sequentially. The default-value for `gracefulShutdown` is false, so running tasks will get interrupted when the web-application gets stopped; resulting in a quicker server-shutdown.

The also configured Listener contains all the necessary logic to create and stop the `ExecutorService`. This must be configured if you want to use the builtin-solution for retrieving an `ExecutorService`.

The `ExecutorService` that gets created by the Listener can be accessed by the `BackgroundExecutor` via the `ContextExecutorServiceProvider`. Just create an instance and pass it as constructor-argument into the `BackgroundExecutor`.

## Create a `VaadinWorker` ##
A `VaadinWorker` represents a task that should be executed in the background and update the UI after work has finished. The abstract class `VaadinWorker` exhibits two methods that encode these two steps: `runInBackground()` and `updateUI()`.

The former method is not synchronized on the `Application` so that it is inherently unsafe to do UI-updates from here. It is possible, but we will get back to this later. In this method you should do all long-running tasks you want to be encoded by the VaadinWorker.

`updateUI()` on the other hand is indeed synchronized on `Application` and can therefore be used to update the frontend. Note that all UI-updates inside an application must be synchronized against the `Application`-instance, which makes this instance's lock a possible bottleneck regarding render-times. It is therefore important to keep your `updateUI()`-method as short and quick as possible. You also need to be aware of possible deadlocks when accessing other code that might also be synchronized using the same `Application`-instance. It is a best practice to not have any heavy logic in this method but instead update the UI using `runInBackground`'s results and finish as quick as possible.

### In-between-updates ###
Can be done using `ProgressListener`s. A `ProgressListener`'s actual purpose is it to communicate the made progress of the background-task towards the client. Therefore it normally updates for example the `ProgressIndicator`s on the frontend. But it is also possible to update arbitrary components using it.

To ease the pain, the `ProgressListener`'s `workProgressed(int, String, VaadinWorker)`-method gets called in a block synchronized on the `Application`-instance. The first argument communicates the actual progress in percent, the second describes the current work-step and the third is the `VaadinWorker` that does the actual work.

## Advanced topics ##

### Localization ###
Basically there are two strings that need to be localized: The text "Loading ..." shown by the BackgroundExecutor and the button-caption of the Cancel-Button. The former can be replaced with whatever text you would like to use by setting the caption of `BackgroundExecutor` using one of the appropriate constructors `BackgroundExecutor(ExecutorServiceProvider, String)` or `BackgroundExecutor(ExecutorServiceProvider, String, TaskProgressViewFactory)`.

The cancel-button can be localized by getting the `TaskProgressViewFactory` from the `BackgroundExecutor`, casting it to `DefaultTaskProgressViewFactory` and using it's `setCancelCaption(String)`-method.

### Customizing the view ###
to be done ...