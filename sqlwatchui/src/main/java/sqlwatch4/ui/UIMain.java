package sqlwatch4.ui;


import org.apache.commons.io.IOUtils;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.hibernate.pretty.Formatter;
import sqlwatch4.rebase.com.google.gson.Gson;
import sqlwatch4.ui.model.UIModel;
import sqlwatch4.ui.model.UITrace;
import sqlwatch4.ui.model.UITracesSlice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author dmitry.mamonov
 */
public class UIMain implements Application {
    private UIModel model = new UIModel();
    private Window window = null;
    @WTKX
    protected ListView sessionsListView;
    @WTKX
    protected PushButton buttonClearSlicesList;

    @WTKX
    protected TableView tableViewPlain;

    @WTKX
    protected TextArea textAreaQueryDetails;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        try {
            WTKXSerializer wtkxSerializer = new WTKXSerializer();
            window = (Window) wtkxSerializer.readObject(this, "./markup/frame.wtkx");
            {
                WTKXSerializer wtkxRoot = wtkxSerializer.getSerializer("root_panel");
                {
                    WTKXSerializer wtkx = wtkxRoot.getSerializer("sessions_list");
                    wtkx.bind(this);
                    sessionsListView.setListData(model.getSlices());
                    sessionsListView.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {
                        @Override
                        public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
                            updateTracesSelection(listView);
                        }

                        @Override
                        public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
                            updateTracesSelection(listView);
                        }

                        @Override
                        public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
                            updateTracesSelection(listView);
                        }

                        @SuppressWarnings({"unchecked"})
                        private void updateTracesSelection(ListView listView) {
                            model.setSelectedSlices((Sequence<UIModel.UISlice>) listView.getSelectedItems());
                        }
                    });

                    buttonClearSlicesList.setAction(new Action() {
                        @Override
                        public void perform(Component source) {
                            model.clear();
                        }
                    });
                }
                {
                    WTKXSerializer wtkxWatch = wtkxRoot.getSerializer("watch_list");
                    {
                        WTKXSerializer wtkx = wtkxWatch.getSerializer("watch_plain_panel");
                        wtkx.bind(this);
                        {
                            WTKXSerializer wtkxDetails = wtkx.getSerializer("query_details");
                            wtkxDetails.bind(this);
                        }
                        tableViewPlain.setTableData(model.getTraces());
                        tableViewPlain.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
                            @Override
                            public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
                                processSelected(tableView);
                            }

                            @Override
                            public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
                                processSelected(tableView);
                            }

                            @Override
                            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                                processSelected(tableView);
                            }

                            private void processSelected(TableView tableView) {
                                //TODO: (x-pivot) it looks like selection is not properly dropped all the time.
                                UITrace selectedTrace = (UITrace) tableView.getSelectedRow();
                                System.out.println("SELECTED COUND: "+tableView.getSelectedRows().getLength());
                                String sql = selectedTrace != null ? String.valueOf(selectedTrace.getSql()) : "-- not selected.";
                                Formatter f = new Formatter(sql);
                                f.setInitialString("");
                                f.setIndentString("  ");
                                String formattedSql = f.format();
                                if (selectedTrace.getStackTrace()!=null){
                                    StringBuilder myLines = new StringBuilder();
                                    for(String line:selectedTrace.getStackTrace().split("\n")){
                                        String trimLine = line.trim();
                                        if (trimLine.startsWith("at com.muranosoft")){
                                            myLines.append(trimLine+"\n");
                                        }
                                    }
                                    if (myLines.length()>0){
                                        formattedSql+="\n\n"+myLines.toString();
                                    }
                                    formattedSql+="\n\n"+selectedTrace.getStackTrace();
                                }
                                textAreaQueryDetails.setText(formattedSql);
                            }
                        });
                    }
                }
            }
            window.open(display);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        RecurrentTask recurrentTask = new RecurrentTask(model);
        recurrentTask.execute(new TaskListener<Integer>() {
            @Override
            public void taskExecuted(Task<Integer> integerTask) {
                repeat(integerTask);
            }

            @Override
            public void executeFailed(Task<Integer> integerTask) {
                repeat(integerTask);
            }

            private void repeat(Task<Integer> integerTask) {
                try {
                    Thread.sleep(100);
                    integerTask.execute(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //TODO [DM] exit.
                }
            }
        });

    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(UIMain.class, args);

    }
}

class RecurrentTask extends Task<Integer> {
    UIModel model;

    RecurrentTask(UIModel model) {
        this.model = model;
    }

    @Override
    public Integer execute() throws TaskExecutionException {
        try {
            GetQuery query = new GetQuery("127.0.0.1", 8666, "/traces", false);
            query.setSerializer(new Serializer() {
                private Gson gson = new Gson();

                @Override
                public Object readObject(InputStream inputStream) throws IOException, SerializationException {
                    String json = IOUtils.toString(inputStream, "UTF-8");
                    return gson.fromJson(json, UITracesSlice.class);
                }

                @Override
                public void writeObject(Object o, OutputStream outputStream) throws IOException, SerializationException {
                    throw new UnsupportedOperationException("Read only!");
                }

                @Override
                public String getMIMEType(Object o) {
                    throw new UnsupportedOperationException("Read only!");
                }
            });
            query.setTimeout(1000);
            Object json = query.execute();
            UITracesSlice tracesSlice = (UITracesSlice) json;
            //System.out.println(new Gson().toJson(tracesSlice));
            model.insert(tracesSlice.getTraces());
            return tracesSlice.getTraces().size();
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
            e.printStackTrace();
            throw new TaskExecutionException(e);
        }
    }
}